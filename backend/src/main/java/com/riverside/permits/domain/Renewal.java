package com.riverside.permits.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record Renewal(String id, String permitNumber, LocalDate previousEndDate,
        LocalDate newEndDate, BigDecimal fee, Instant createdAt) {
    public Renewal {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (permitNumber == null || permitNumber.isBlank()) throw new IllegalArgumentException("permitNumber must not be blank");
        Objects.requireNonNull(previousEndDate, "previousEndDate");
        Objects.requireNonNull(newEndDate, "newEndDate");
        Objects.requireNonNull(fee, "fee");
        Objects.requireNonNull(createdAt, "createdAt");
        if (!newEndDate.isAfter(previousEndDate)) throw new IllegalArgumentException("newEndDate must be after previousEndDate");
        if (fee.signum() < 0) throw new IllegalArgumentException("fee must not be negative");
    }
}
