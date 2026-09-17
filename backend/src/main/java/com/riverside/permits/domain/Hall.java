package com.riverside.permits.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Hall(String id, String name, String district, BigDecimal dailyRate, boolean active) {
    public Hall {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (district == null || district.isBlank()) throw new IllegalArgumentException("district must not be blank");
        Objects.requireNonNull(dailyRate, "dailyRate");
        if (dailyRate.signum() < 0) throw new IllegalArgumentException("dailyRate must not be negative");
    }
}
