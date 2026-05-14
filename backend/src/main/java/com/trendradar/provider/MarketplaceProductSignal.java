package com.trendradar.provider;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketplaceProductSignal(
    String id,
    String title,
    String categoryName,
    String imageUrl,
    String itemWebUrl,
    BigDecimal price,
    String currency,
    String condition,
    String sellerUsername,
    String sellerFeedbackPercentage,
    String itemLocationCountry,
    boolean topRatedBuyingExperience,
    boolean priorityListing,
    Instant observedAt
) {
}
