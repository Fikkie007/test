package com.riverside.permits.domain;

import java.time.Instant;

public record PermitHistory(String id, String permitNumber, String eventType,
        String description, Instant occurredAt) {
    public PermitHistory {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (permitNumber == null || permitNumber.isBlank()) throw new IllegalArgumentException("permitNumber must not be blank");
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("eventType must not be blank");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description must not be blank");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt must not be null");
    }
}
