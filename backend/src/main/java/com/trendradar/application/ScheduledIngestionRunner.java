package com.trendradar.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledIngestionRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledIngestionRunner.class);

    private final IngestionProperties ingestionProperties;
    private final IngestionOrchestrationService ingestionOrchestrationService;

    public ScheduledIngestionRunner(
        IngestionProperties ingestionProperties,
        IngestionOrchestrationService ingestionOrchestrationService
    ) {
        this.ingestionProperties = ingestionProperties;
        this.ingestionOrchestrationService = ingestionOrchestrationService;
    }

    @Scheduled(fixedRateString = "#{@scheduledIngestionFixedRateMs}")
    public void runScheduledIngestion() {
        if (!ingestionProperties.enabled()) {
            LOGGER.debug("Scheduled ingestion skipped because scheduler is disabled");
            return;
        }

        LOGGER.info("Scheduled ingestion started");
        ingestionOrchestrationService.runEnabledSeedTerms(null, null);
    }
}
