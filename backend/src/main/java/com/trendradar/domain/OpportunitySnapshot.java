package com.trendradar.domain;

import java.time.Instant;
import java.util.List;

public record OpportunitySnapshot(
    ProductConcept productConcept,
    Niche niche,
    Region region,
    int score,
    String scoreLabel,
    int marketplaceProofScore,
    int priceViabilityScore,
    int freshnessScore,
    int sellerQualityScore,
    int shippingRiskScore,
    int competitionRiskScore,
    int finalScore,
    MarketplaceEvidence marketplaceEvidence,
    List<SourceEvidence> sourceEvidence,
    List<RiskSignal> risks,
    String explanation,
    Instant generatedAt
) {
}
