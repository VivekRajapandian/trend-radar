package com.trendradar.application;

public record SeedTermRequest(
    String niche,
    String region,
    String searchTerm,
    Boolean enabled,
    Integer priority,
    String sourceType
) {
}
