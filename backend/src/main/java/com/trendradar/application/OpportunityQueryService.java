package com.trendradar.application;

import com.trendradar.domain.Niche;
import com.trendradar.domain.OpportunitySnapshot;
import com.trendradar.domain.Region;
import com.trendradar.infrastructure.persistence.OpportunityPersistenceService;
import com.trendradar.infrastructure.persistence.ProviderRunEntity;
import com.trendradar.infrastructure.persistence.ScoringRunEntity;
import com.trendradar.infrastructure.persistence.SourceRecordEntity;
import com.trendradar.normalization.OpportunityNormalizer;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketSignalProvider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpportunityQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpportunityQueryService.class);

    private final List<MarketSignalProvider> marketSignalProviders;
    private final OpportunityNormalizer opportunityNormalizer;
    private final OpportunityPersistenceService opportunityPersistenceService;

    public OpportunityQueryService(
        List<MarketSignalProvider> marketSignalProviders,
        OpportunityNormalizer opportunityNormalizer,
        OpportunityPersistenceService opportunityPersistenceService
    ) {
        this.marketSignalProviders = marketSignalProviders;
        this.opportunityNormalizer = opportunityNormalizer;
        this.opportunityPersistenceService = opportunityPersistenceService;
    }

    @Transactional
    public List<OpportunitySnapshot> findOpportunities(String nicheCode, String regionCode) {
        Niche niche = Niche.fromCode(nicheCode);
        Region region = Region.fromCode(regionCode);
        List<OpportunitySnapshot> latest = opportunityPersistenceService.findLatest(niche, region);

        if (!latest.isEmpty()) {
            return latest;
        }

        return refreshOpportunities(nicheCode, regionCode);
    }

    @Transactional
    public List<OpportunitySnapshot> refreshOpportunities(String nicheCode, String regionCode) {
        LOGGER.info("Refresh requested for niche {} and region {}", nicheCode, regionCode);
        Niche niche = Niche.fromCode(nicheCode);
        Region region = Region.fromCode(regionCode);
        ProviderRunResult providerRunResult = fetchFirstAvailableSignalBatch(niche, region);
        MarketSignalBatch signalBatch = providerRunResult.signalBatch();

        List<SourceRecordEntity> sourceRecords = opportunityPersistenceService.saveSourceRecords(
            providerRunResult.providerRun(),
            signalBatch
        );
        opportunityPersistenceService.saveNormalizedSignals(
            providerRunResult.providerRun(),
            sourceRecords,
            signalBatch,
            niche,
            region
        );

        ScoringRunEntity scoringRun = opportunityPersistenceService.startScoringRun(providerRunResult.providerRun());

        try {
            LOGGER.info(
                "Scoring provider run {} with {} normalized product signals",
                providerRunResult.providerRun().getId(),
                signalBatch.products().size()
            );
            List<OpportunitySnapshot> opportunities = opportunityNormalizer.normalize(signalBatch, niche, region);
            opportunityPersistenceService.saveOpportunitySnapshots(providerRunResult.providerRun(), scoringRun, opportunities);
            opportunityPersistenceService.completeScoringRun(scoringRun, opportunities.size());
            LOGGER.info(
                "Refresh completed for provider run {} with {} opportunities",
                providerRunResult.providerRun().getId(),
                opportunities.size()
            );

            return opportunities;
        } catch (RuntimeException exception) {
            opportunityPersistenceService.failScoringRun(scoringRun, exception);
            throw exception;
        }
    }

    private ProviderRunResult fetchFirstAvailableSignalBatch(Niche niche, Region region) {
        for (MarketSignalProvider marketSignalProvider : marketSignalProviders) {
            if (!marketSignalProvider.isAvailable()) {
                continue;
            }

            ProviderRunEntity providerRun = opportunityPersistenceService.startProviderRun(
                marketSignalProvider.sourceType(),
                marketSignalProvider.queryFor(niche, region),
                niche,
                region
            );

            try {
                LOGGER.info(
                    "Executing provider {} for niche {} and region {}",
                    marketSignalProvider.sourceType(),
                    niche.code(),
                    region.code()
                );
                MarketSignalBatch signalBatch = marketSignalProvider.fetchSignals(niche, region);
                opportunityPersistenceService.completeProviderRun(providerRun, signalBatch);

                if (!signalBatch.products().isEmpty()) {
                    LOGGER.info(
                        "Provider {} completed run {} with {} records",
                        marketSignalProvider.sourceType(),
                        providerRun.getId(),
                        signalBatch.products().size()
                    );
                    return new ProviderRunResult(providerRun, signalBatch);
                }
            } catch (RuntimeException exception) {
                opportunityPersistenceService.failProviderRun(providerRun, exception);
                LOGGER.warn(
                    "Provider {} failed for niche {} and region {}: {}",
                    marketSignalProvider.getClass().getSimpleName(),
                    niche.code(),
                    region.code(),
                    exception.getMessage()
                );
            }
        }

        throw new IllegalStateException("No market signal provider returned opportunity signals");
    }

    private record ProviderRunResult(ProviderRunEntity providerRun, MarketSignalBatch signalBatch) {
    }
}
