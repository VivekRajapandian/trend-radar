package com.trendradar.domain;

import java.math.BigDecimal;

public record MarketplaceEvidence(
    int estimatedSoldCount,
    int activeListings,
    BigDecimal medianPrice,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    String demandSignal
) {
}
