package com.trendradar.scoring;

public record OpportunityScore(
    int marketplaceProofScore,
    int priceViabilityScore,
    int freshnessScore,
    int sellerQualityScore,
    int shippingRiskScore,
    int competitionRiskScore,
    int finalScore,
    String label
) {

    public int value() {
        return finalScore;
    }
}
