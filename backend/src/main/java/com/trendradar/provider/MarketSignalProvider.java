package com.trendradar.provider;

import com.trendradar.domain.Niche;
import com.trendradar.domain.Region;
import java.util.List;

public interface MarketSignalProvider {

    MarketSignalBatch fetchSignals(Niche niche, Region region);

    default MarketSignalBatch fetchSignals(Niche niche, Region region, String searchTerm) {
        return fetchSignals(niche, region);
    }

    String sourceType();

    String queryFor(Niche niche, Region region);

    default String queryFor(Niche niche, Region region, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return queryFor(niche, region);
        }

        return searchTerm;
    }

    default boolean isAvailable() {
        return true;
    }
}
