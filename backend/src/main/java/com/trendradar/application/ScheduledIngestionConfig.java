package com.trendradar.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduledIngestionConfig {

    @Bean
    public String scheduledIngestionFixedRateMs(IngestionProperties ingestionProperties) {
        long minutes = Math.max(1, ingestionProperties.fixedRateMinutes());
        return String.valueOf(minutes * 60_000L);
    }
}
