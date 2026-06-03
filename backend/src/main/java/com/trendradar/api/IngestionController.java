package com.trendradar.api;

import com.trendradar.application.IngestionOrchestrationService;
import com.trendradar.application.IngestionRunSummary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestionController {

    private final IngestionOrchestrationService ingestionOrchestrationService;

    public IngestionController(IngestionOrchestrationService ingestionOrchestrationService) {
        this.ingestionOrchestrationService = ingestionOrchestrationService;
    }

    @PostMapping("/api/ingestion/run")
    public IngestionRunSummary runIngestion(
        @RequestParam(required = false) String niche,
        @RequestParam(required = false) String region
    ) {
        return ingestionOrchestrationService.runEnabledSeedTerms(niche, region);
    }
}
