package com.trendradar.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trendradar.scheduler")
public record IngestionProperties(
    boolean enabled,
    long fixedRateMinutes
) {

    public IngestionProperties {
        if (fixedRateMinutes <= 0) {
            fixedRateMinutes = 360;
        }
    }
}
