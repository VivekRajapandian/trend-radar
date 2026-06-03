package com.trendradar.application;

import java.time.Instant;

public record SeedTermResponse(
    Long id,
    String niche,
    String region,
    String searchTerm,
    boolean enabled,
    int priority,
    String sourceType,
    Instant createdAt,
    Instant updatedAt
) {
}
