package com.riverside.permits.service;

import com.riverside.permits.domain.Hall;
import com.riverside.permits.domain.Permit;
import com.riverside.permits.domain.PermitHistory;
import com.riverside.permits.domain.PermitStatus;
import com.riverside.permits.domain.Purpose;
import com.riverside.permits.domain.Renewal;
import com.riverside.permits.repository.HallRepository;
import com.riverside.permits.repository.PermitHistoryRepository;
import com.riverside.permits.repository.PermitRepository;
import com.riverside.permits.repository.RenewalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermitService {
    private static final int MAX_PAGE_SIZE = 100;
    private final PermitRepository permits;
    private final HallRepository halls;
    private final RenewalRepository renewals;
    private final PermitHistoryRepository history;

    public PermitService(
            PermitRepository permits,
            HallRepository halls,
            RenewalRepository renewals,
            PermitHistoryRepository history) {
        this.permits = permits;
        this.halls = halls;
        this.renewals = renewals;
        this.history = history;
    }

    // In-memory storage has no database transaction; serialize service reads and writes so a renewal is never observed halfway through.
    public synchronized Permit get(String number) {
        return permits.findByNumber(number).orElseThrow(() -> missing(number));
    }

    public synchronized SearchResult search(SearchCriteria criteria, int page, int size) {
        validatePage(page, size, criteria.from(), criteria.to());

        List<Permit> matches = permits.findAll().stream()
                .filter(criteria.predicate())
                .sorted(Comparator.comparing((Permit permit) -> permit.createdAt()).reversed())
                .toList();
        long offset = (long) page * size;
        int start = (int) Math.min(offset, matches.size());
        int end = (int) Math.min((long) start + size, matches.size());
        int totalPages = (int) ((matches.size() + (long) size - 1) / size);

        return new SearchResult(matches.subList(start, end), page, size, matches.size(), totalPages);
    }

    public Hall hall(Permit permit) {
        return halls.findById(permit.hallId())
                .orElseThrow(() -> new IllegalStateException("Permit hall is unavailable"));
    }

    public SummaryData summary(Permit permit) {
        return new SummaryData(
                permit.permitNumber(),
                permit.holderName(),
                hall(permit).name(),
                permit.purpose().displayName(),
                permit.status().name(),
                permit.startDate(),
                permit.endDate(),
                permit.originalFee());
    }

    public synchronized DetailData detail(String number) {
        Permit permit = get(number);
        Hall hall = hall(permit);
        return new DetailData(
                permit.permitNumber(),
                permit.holderName(),
                new HallData(hall.id(), hall.name(), hall.district()),
                permit.purpose().displayName(),
                permit.status().name(),
                permit.startDate(),
                permit.endDate(),
                permit.originalFee(),
                renewals(number),
                history(number));
    }

    public synchronized List<Renewal> renewals(String number) {
        return renewals.findByPermitNumber(number).stream()
                .sorted(Comparator.comparing((Renewal renewal) -> renewal.createdAt()).reversed())
                .toList();
    }

    public synchronized List<PermitHistory> history(String number) {
        return history.findByPermitNumber(number).stream()
                .sorted(Comparator.comparing((PermitHistory entry) -> entry.occurredAt()).reversed())
                .toList();
    }

    private void validatePage(int page, int size, LocalDate from, LocalDate to) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE || (from != null && to != null && from.isAfter(to))) {
            throw bad("INVALID_SEARCH", "Search parameters are invalid.", null);
        }
    }

    private ResponseStatusException missing(String number) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Permit not found: " + number);
    }

    private ApiException bad(String code, String message, String field) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message, field);
    }

    public record SearchCriteria(
            String permitNumber,
            String holderName,
            String hallId,
            Purpose purpose,
            PermitStatus status,
            LocalDate from,
            LocalDate to) {
        Predicate<Permit> predicate() {
            String normalizedHolder = holderName == null ? null : holderName.toLowerCase(Locale.ROOT);
            return permit -> (permitNumber == null || permit.permitNumber().equals(permitNumber))
                    && (normalizedHolder == null
                            || permit.holderName().toLowerCase(Locale.ROOT).contains(normalizedHolder))
                    && (hallId == null || permit.hallId().equals(hallId))
                    && (purpose == null || permit.purpose() == purpose)
                    && (status == null || permit.status() == status)
                    && (from == null || !permit.endDate().isBefore(from))
                    && (to == null || !permit.startDate().isAfter(to));
        }
    }

    public record SearchResult(List<Permit> permits, int page, int size, int totalElements, int totalPages) { }

    public record SummaryData(
            String permitNumber,
            String holderName,
            String hall,
            String purpose,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal fee) { }

    public record DetailData(
            String permitNumber,
            String holderName,
            HallData hall,
            String purpose,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal fee,
            List<Renewal> renewalHistory,
            List<PermitHistory> history) { }

    public record HallData(String id, String name, String district) { }

}
