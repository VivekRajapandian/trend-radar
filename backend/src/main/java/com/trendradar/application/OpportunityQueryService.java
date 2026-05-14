package com.trendradar.application;

import com.trendradar.domain.Niche;
import com.trendradar.domain.OpportunitySnapshot;
import com.trendradar.domain.Region;
import com.trendradar.normalization.OpportunityNormalizer;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketSignalProvider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpportunityQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpportunityQueryService.class);

    private final List<MarketSignalProvider> marketSignalProviders;
    private final OpportunityNormalizer opportunityNormalizer;

    public OpportunityQueryService(
        List<MarketSignalProvider> marketSignalProviders,
        OpportunityNormalizer opportunityNormalizer
    ) {
        this.marketSignalProviders = marketSignalProviders;
        this.opportunityNormalizer = opportunityNormalizer;
    }

    public List<OpportunitySnapshot> findOpportunities(String nicheCode, String regionCode) {
        Niche niche = Niche.fromCode(nicheCode);
        Region region = Region.fromCode(regionCode);
        MarketSignalBatch signalBatch = fetchFirstAvailableSignalBatch(niche, region);

        return opportunityNormalizer.normalize(signalBatch, niche, region);
    }

    private MarketSignalBatch fetchFirstAvailableSignalBatch(Niche niche, Region region) {
        for (MarketSignalProvider marketSignalProvider : marketSignalProviders) {
            if (!marketSignalProvider.isAvailable()) {
                continue;
            }

            try {
                MarketSignalBatch signalBatch = marketSignalProvider.fetchSignals(niche, region);

                if (!signalBatch.products().isEmpty()) {
                    return signalBatch;
                }
            } catch (RuntimeException exception) {
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
}
