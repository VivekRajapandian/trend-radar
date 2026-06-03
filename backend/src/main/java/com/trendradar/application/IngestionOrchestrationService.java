package com.trendradar.application;

import com.trendradar.domain.Region;
import com.trendradar.domain.OpportunitySnapshot;
import com.trendradar.infrastructure.persistence.ProviderRunRepository;
import com.trendradar.infrastructure.persistence.SeedTermEntity;
import com.trendradar.infrastructure.persistence.SeedTermRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionOrchestrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionOrchestrationService.class);

    private final SeedTermRepository seedTermRepository;
    private final ProviderRunRepository providerRunRepository;
    private final OpportunityQueryService opportunityQueryService;

    public IngestionOrchestrationService(
        SeedTermRepository seedTermRepository,
        ProviderRunRepository providerRunRepository,
        OpportunityQueryService opportunityQueryService
    ) {
        this.seedTermRepository = seedTermRepository;
        this.providerRunRepository = providerRunRepository;
        this.opportunityQueryService = opportunityQueryService;
    }

    public IngestionRunSummary runEnabledSeedTerms(String niche, String region) {
        Instant startedAt = Instant.now();
        List<SeedTermEntity> seedTerms = findEnabledSeedTerms(niche, region);
        int successfulRuns = 0;
        int failedRuns = 0;
        int totalRecordsFetched = 0;
        int opportunitiesGenerated = 0;
        List<String> errors = new ArrayList<>();

        LOGGER.info("Starting ingestion run for {} enabled seed terms", seedTerms.size());

        for (SeedTermEntity seedTerm : seedTerms) {
            try {
                LOGGER.info(
                    "Ingesting seed term {} for niche {} and region {}",
                    seedTerm.getSearchTerm(),
                    seedTerm.getNiche(),
                    seedTerm.getRegion()
                );
                List<OpportunitySnapshot> opportunities = opportunityQueryService.refreshOpportunities(
                    seedTerm.getNiche(),
                    seedTerm.getRegion(),
                    seedTerm.getSearchTerm()
                );
                successfulRuns++;
                opportunitiesGenerated += opportunities.size();
                totalRecordsFetched += providerRunRepository.findFirstByOrderByStartedAtDesc()
                    .map(run -> run.getRecordsFetched())
                    .orElse(0);
            } catch (RuntimeException exception) {
                failedRuns++;
                String error = "%s/%s '%s': %s".formatted(
                    seedTerm.getNiche(),
                    seedTerm.getRegion(),
                    seedTerm.getSearchTerm(),
                    exception.getMessage()
                );
                errors.add(error);
                LOGGER.warn("Seed term ingestion failed: {}", error);
            }
        }

        Instant completedAt = Instant.now();
        LOGGER.info(
            "Completed ingestion run: {} successful, {} failed, {} records, {} opportunities",
            successfulRuns,
            failedRuns,
            totalRecordsFetched,
            opportunitiesGenerated
        );

        return new IngestionRunSummary(
            startedAt,
            completedAt,
            seedTerms.size(),
            successfulRuns,
            failedRuns,
            totalRecordsFetched,
            opportunitiesGenerated,
            errors
        );
    }

    @Transactional(readOnly = true)
    public Instant latestIngestionRunAt() {
        return providerRunRepository.findFirstByOrderByStartedAtDesc()
            .map(run -> run.getStartedAt())
            .orElse(null);
    }

    private List<SeedTermEntity> findEnabledSeedTerms(String niche, String region) {
        if (hasText(niche) && hasText(region)) {
            return seedTermRepository.findByEnabledTrueAndNicheAndRegionOrderByPriorityDescCreatedAtAsc(
                niche.trim(),
                Region.fromCode(region).code()
            );
        }

        return seedTermRepository.findByEnabledTrueOrderByPriorityDescCreatedAtAsc();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
