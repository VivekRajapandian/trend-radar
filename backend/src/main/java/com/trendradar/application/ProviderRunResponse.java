package com.trendradar.application;

import java.time.Instant;

public record ProviderRunResponse(
    Long id,
    String provider,
    String source,
    String niche,
    String region,
    String query,
    String status,
    Instant startedAt,
    Instant completedAt,
    Long durationMs,
    int recordsFetched,
    int opportunitiesGenerated,
    String scoringVersion,
    String errorMessage,
    Instant createdAt
) {
}
