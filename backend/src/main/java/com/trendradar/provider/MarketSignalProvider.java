package com.trendradar.provider;

import com.trendradar.domain.Niche;
import com.trendradar.domain.Region;
import java.util.List;

public interface MarketSignalProvider {

    MarketSignalBatch fetchSignals(Niche niche, Region region);

    default boolean isAvailable() {
        return true;
    }
}
