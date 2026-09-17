package com.riverside.permits.service;

import com.riverside.permits.domain.Permit;
import com.riverside.permits.domain.PermitStatus;
import com.riverside.permits.domain.Purpose;
import com.riverside.permits.domain.Renewal;
import com.riverside.permits.domain.PermitHistory;
import com.riverside.permits.repository.PermitHistoryRepository;
import com.riverside.permits.repository.PermitRepository;
import com.riverside.permits.repository.RenewalRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RenewalService {
    private static final int RENEWAL_FEE_DAY_CAP = 30;
    private static final int EXPIRED_RENEWAL_LIMIT_DAYS = 90;

    private final PermitRepository permits;
    private final RenewalRepository renewals;
    private final PermitHistoryRepository history;
    private final PermitService permitService;
    private final Map<String, Quote> quotes = new ConcurrentHashMap<>();
    private final Clock clock;

    public RenewalService(
            PermitRepository permits,
            RenewalRepository renewals,
            PermitHistoryRepository history,
            PermitService permitService,
            Clock clock) {
        this.permits = permits;
        this.renewals = renewals;
        this.history = history;
        this.permitService = permitService;
        this.clock = clock;
    }

    public Quote quote(String number, LocalDate newEndDate) {
        synchronized (permitService) {
            Permit permit = permitService.get(number);
            validateDate(permit, newEndDate);
            validateEligibility(permit);

            long addedDays = ChronoUnit.DAYS.between(permit.endDate(), newEndDate);
            Quote quote = new Quote(
                    UUID.randomUUID().toString(),
                    number,
                    permit.version(),
                    permit.endDate(),
                    newEndDate,
                    Math.toIntExact(addedDays),
                    fee(permit, addedDays));
            quotes.put(quote.quoteId(), quote);
            return quote;
        }
    }

    // The permit service monitor is the transaction boundary for this in-memory store.
    public PermitService.DetailData confirm(String number, LocalDate newEndDate, String quoteId) {
        synchronized (permitService) {
            Permit permit = permitService.get(number);
            validateDate(permit, newEndDate);
            validateEligibility(permit);
            Quote quote = quotes.get(quoteId);
            validateQuote(quote, number, newEndDate, permit);

            long addedDays = ChronoUnit.DAYS.between(permit.endDate(), newEndDate);
            BigDecimal fee = fee(permit, addedDays);
            String renewalId = UUID.randomUUID().toString();
            String historyId = UUID.randomUUID().toString();

            try {
                Instant now = Instant.now(clock);
                renewals.save(new Renewal(renewalId, number, permit.endDate(), newEndDate, fee, now));
                permits.save(permit.renew(newEndDate, fee));
                history.save(new PermitHistory(historyId, number, "RENEWED", "Renewed to " + newEndDate, now));
                quotes.remove(quoteId);
                return permitService.detail(number);
            } catch (RuntimeException exception) {
                permits.save(permit);
                renewals.delete(renewalId);
                history.delete(historyId);
                throw new IllegalStateException("Renewal could not be saved", exception);
            }
        }
    }

    private void validateQuote(Quote quote, String number, LocalDate newEndDate, Permit permit) {
        if (quote == null
                || !quote.permitNumber().equals(number)
                || quote.version() != permit.version()
                || !quote.newEndDate().equals(newEndDate)
                || !quote.previousEndDate().equals(permit.endDate())) {
            throw conflict(
                    "RENEWAL_VERSION_CONFLICT",
                    "The permit changed since the quote was displayed.",
                    null);
        }
    }

    private void validateDate(Permit permit, LocalDate newEndDate) {
        if (newEndDate == null || !newEndDate.isAfter(permit.endDate())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "RENEWAL_DATE_NOT_AFTER_CURRENT_END",
                    "The new end date must be after " + permit.endDate() + ".",
                    "newEndDate");
        }
    }

    private void validateEligibility(Permit permit) {
        if (permit.status() == PermitStatus.EXPIRED
                && permit.endDate().plusDays(EXPIRED_RENEWAL_LIMIT_DAYS).isBefore(LocalDate.now(clock))) {
            throw conflict(
                    "RENEWAL_EXPIRED_TOO_LONG",
                    "This permit expired more than 90 days ago.",
                    null);
        }
        if (permit.status() != PermitStatus.ACTIVE && permit.status() != PermitStatus.EXPIRED) {
            throw conflict(
                    "RENEWAL_STATUS_NOT_ELIGIBLE",
                    "This permit cannot be renewed in its current status.",
                    null);
        }
    }

    private BigDecimal fee(Permit permit, long addedDays) {
        if (permit.purpose() == Purpose.COUNCIL_USE) {
            return BigDecimal.ZERO.setScale(2);
        }
        return permitService.hall(permit).dailyRate()
                .multiply(BigDecimal.valueOf(Math.min(addedDays, RENEWAL_FEE_DAY_CAP)));
    }

    private ApiException conflict(String code, String message, String field) {
        return new ApiException(HttpStatus.CONFLICT, code, message, field);
    }

    public record Quote(
            String quoteId,
            String permitNumber,
            int version,
            LocalDate previousEndDate,
            LocalDate newEndDate,
            int addedDays,
            BigDecimal fee) {
        public boolean paymentRequired() {
            return fee.signum() > 0;
        }
    }
}
