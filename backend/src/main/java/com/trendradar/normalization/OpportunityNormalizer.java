package com.trendradar.normalization;

import com.trendradar.domain.Niche;
import com.trendradar.domain.OpportunitySnapshot;
import com.trendradar.domain.Region;
import com.trendradar.provider.MarketSignalBatch;
import java.util.List;

public interface OpportunityNormalizer {

    List<OpportunitySnapshot> normalize(MarketSignalBatch signalBatch, Niche niche, Region region);
}
