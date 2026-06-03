package com.trendradar.application;

import java.time.Instant;
import java.util.List;

public record IngestionRunSummary(
    Instant startedAt,
    Instant completedAt,
    int totalSeedTerms,
    int successfulRuns,
    int failedRuns,
    int totalRecordsFetched,
    int opportunitiesGenerated,
    List<String> errors
) {
}
