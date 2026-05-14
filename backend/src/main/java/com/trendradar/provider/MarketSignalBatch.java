package com.trendradar.provider;

import java.util.List;

public record MarketSignalBatch(
    String sourceType,
    String query,
    int totalMatches,
    List<MarketplaceProductSignal> products
) {
}
