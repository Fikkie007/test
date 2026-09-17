package com.riverside.permits.controller;

import com.riverside.permits.domain.PermitHistory;
import com.riverside.permits.domain.PermitStatus;
import com.riverside.permits.domain.Purpose;
import com.riverside.permits.domain.Renewal;
import com.riverside.permits.service.PermitService;
import com.riverside.permits.service.RenewalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permits")
public class PermitController {
    private final PermitService service;
    private final RenewalService renewalService;

    public PermitController(PermitService service, RenewalService renewalService) {
        this.service = service;
        this.renewalService = renewalService;
    }

    @GetMapping
    public PageResult search(
            @RequestParam(required = false) String permitNumber,
            @RequestParam(required = false) String holderName,
            @RequestParam(required = false) String hallId,
            @RequestParam(required = false) Purpose purpose,
            @RequestParam(required = false) PermitStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PermitService.SearchResult result = service.search(
                new PermitService.SearchCriteria(permitNumber, holderName, hallId, purpose, status, from, to),
                page,
                size);
        List<Summary> summaries = result.permits().stream()
                .map(service::summary)
                .map(this::summary)
                .toList();
        return new PageResult(summaries, result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/{number}")
    public Detail detail(@PathVariable String number) {
        return detail(service.detail(number));
    }

    @PostMapping("/{number}/renewal-quote")
    public QuoteResponse quote(@PathVariable String number, @Valid @RequestBody RenewalRequest request) {
        return QuoteResponse.of(renewalService.quote(number, request.newEndDate()));
    }

    @PostMapping("/{number}/renewals")
    public Detail renew(@PathVariable String number, @Valid @RequestBody RenewalConfirmation request) {
        return detail(renewalService.confirm(number, request.newEndDate(), request.quoteId()));
    }

    private Summary summary(PermitService.SummaryData summary) {
        return new Summary(
                summary.permitNumber(),
                summary.holderName(),
                summary.hall(),
                summary.purpose(),
                summary.status(),
                summary.startDate(),
                summary.endDate(),
                summary.fee());
    }

    private Detail detail(PermitService.DetailData detail) {
        PermitService.HallData hall = detail.hall();
        return new Detail(
                detail.permitNumber(),
                detail.holderName(),
                new HallView(hall.id(), hall.name(), hall.district()),
                detail.purpose(),
                detail.status(),
                detail.startDate(),
                detail.endDate(),
                detail.fee(),
                detail.renewalHistory(),
                detail.history());
    }

    public record RenewalRequest(@NotNull LocalDate newEndDate) { }

    public record RenewalConfirmation(@NotNull LocalDate newEndDate, @NotBlank String quoteId) { }

    public record QuoteResponse(
            String quoteId,
            String permitNumber,
            LocalDate previousEndDate,
            LocalDate newEndDate,
            int addedDays,
            BigDecimal fee,
            boolean paymentRequired) {
        static QuoteResponse of(RenewalService.Quote quote) {
            return new QuoteResponse(
                    quote.quoteId(),
                    quote.permitNumber(),
                    quote.previousEndDate(),
                    quote.newEndDate(),
                    quote.addedDays(),
                    quote.fee(),
                    quote.paymentRequired());
        }
    }

    public record PageResult(List<Summary> content, int page, int size, int totalElements, int totalPages) { }

    public record Summary(
            String permitNumber,
            String holderName,
            String hall,
            String purpose,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal fee) { }

    public record Detail(
            String permitNumber,
            String holderName,
            HallView hall,
            String purpose,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal fee,
            List<Renewal> renewalHistory,
            List<PermitHistory> history) { }

    public record HallView(String id, String name, String district) { }
}
