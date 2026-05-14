package com.trendradar.application;

import java.time.Instant;
import java.util.List;

public record SystemStatusResponse(
    String backendStatus,
    String dbConnectivity,
    ProviderRunResponse latestProviderRun,
    ScoringRunSummary latestScoringRun,
    long totalOpportunitiesStored,
    long totalSourceRecordsStored,
    List<ProviderStatus> activeProviders,
    Instant generatedAt
) {

    public record ScoringRunSummary(
        Long id,
        Long providerRunId,
        String status,
        String scoringVersion,
        Instant startedAt,
        Instant completedAt,
        Long durationMs,
        int opportunitiesScored,
        String errorMessage
    ) {
    }

    public record ProviderStatus(
        String sourceType,
        boolean available
    ) {
    }
}
