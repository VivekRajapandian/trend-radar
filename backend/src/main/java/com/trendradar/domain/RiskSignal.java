package com.trendradar.domain;

public record RiskSignal(
    String type,
    String severity,
    String description
) {
}
