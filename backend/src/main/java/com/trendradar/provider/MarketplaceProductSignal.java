package com.trendradar.provider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketplaceProductSignal(
    String id,
    String title,
    String categoryName,
    String imageUrl,
    String itemWebUrl,
    String rawJson,
    BigDecimal price,
    String currency,
    String condition,
    String sellerUsername,
    String sellerFeedbackPercentage,
    Integer sellerFeedbackScore,
    String itemLocationCountry,
    BigDecimal shippingCost,
    String shippingCostType,
    List<String> buyingOptions,
    boolean topRatedBuyingExperience,
    boolean priorityListing,
    Instant itemOriginDate,
    Instant observedAt
) {
}
