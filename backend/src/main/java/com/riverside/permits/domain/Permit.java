package com.riverside.permits.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record Permit(String permitNumber, String holderName, String hallId, Purpose purpose,
        PermitStatus status, LocalDate startDate, LocalDate endDate, BigDecimal originalFee,
        Instant createdAt, int version) {
    public Permit {
        requireText(permitNumber, "permitNumber");
        requireText(holderName, "holderName");
        requireText(hallId, "hallId");
        Objects.requireNonNull(purpose, "purpose");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        Objects.requireNonNull(originalFee, "originalFee");
        Objects.requireNonNull(createdAt, "createdAt");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("endDate must not be before startDate");
        if (originalFee.signum() < 0) throw new IllegalArgumentException("originalFee must not be negative");
        if (version < 0) throw new IllegalArgumentException("version must not be negative");
    }

    public Permit renew(LocalDate newEndDate, BigDecimal fee) {
        return new Permit(permitNumber, holderName, hallId, purpose,
                fee.signum() > 0 ? PermitStatus.AWAITING_PAYMENT : PermitStatus.ACTIVE,
                startDate, newEndDate, originalFee, createdAt, version + 1);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    }
}
