package com.trendradar.domain;

import java.time.Instant;

public record SourceEvidence(
    String sourceType,
    String title,
    double confidence,
    Instant observedAt
) {
}
