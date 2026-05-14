package com.trendradar.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendradar.domain.RiskSignal;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketplaceProductSignal;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpportunityScoringServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-14T00:00:00Z");
    private final OpportunityScoringService scoringService = new OpportunityScoringService(
        Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void scoreRewardsStrongMarketplaceEvidenceAndSellerQuality() {
        MarketplaceProductSignal product = productBuilder()
            .sellerFeedbackPercentage("99.9")
            .sellerFeedbackScore(6000)
            .topRatedBuyingExperience(true)
            .itemOriginDate(NOW.minus(DurationDays.days(12)))
            .build();

        OpportunityScore score = scoringService.score(batch(1800, product), product, List.of());

        assertThat(score.marketplaceProofScore()).isGreaterThanOrEqualTo(45);
        assertThat(score.sellerQualityScore()).isEqualTo(100);
        assertThat(score.freshnessScore()).isEqualTo(100);
        assertThat(score.finalScore()).isGreaterThanOrEqualTo(70);
        assertThat(score.label()).isIn("Promising", "High");
    }

    @Test
    void scorePenalizesExpensiveInternationalCalculatedShipping() {
        MarketplaceProductSignal product = productBuilder()
            .price(new BigDecimal("375.00"))
            .sellerFeedbackPercentage("94.0")
            .sellerFeedbackScore(18)
            .itemLocationCountry("JP")
            .shippingCost(new BigDecimal("48.00"))
            .shippingCostType("CALCULATED")
            .topRatedBuyingExperience(false)
            .itemOriginDate(NOW.minus(DurationDays.days(420)))
            .build();

        OpportunityScore score = scoringService.score(
            batch(9800, product),
            product,
            List.of(new RiskSignal("cross_border_supply", "LOW", "Cross-border shipping may affect margin"))
        );

        assertThat(score.priceViabilityScore()).isLessThanOrEqualTo(50);
        assertThat(score.shippingRiskScore()).isLessThan(45);
        assertThat(score.freshnessScore()).isEqualTo(25);
        assertThat(score.competitionRiskScore()).isLessThanOrEqualTo(45);
        assertThat(score.label()).isIn("Watch", "Weak");
    }

    @Test
    void labelThresholdsMatchMilestoneRules() {
        assertThat(scoringService.score(batch(50, weakProduct()), weakProduct(), List.of()).label())
            .isIn("Weak", "Watch");

        MarketplaceProductSignal strongProduct = productBuilder()
            .sellerFeedbackPercentage("100.0")
            .sellerFeedbackScore(12000)
            .topRatedBuyingExperience(true)
            .itemOriginDate(NOW.minus(DurationDays.days(4)))
            .build();

        assertThat(scoringService.score(batch(1200, strongProduct), strongProduct, List.of()).label())
            .isIn("Promising", "High");
    }

    private MarketSignalBatch batch(int totalMatches, MarketplaceProductSignal product) {
        return new MarketSignalBatch("test", "test query", totalMatches, List.of(product));
    }

    private MarketplaceProductSignal weakProduct() {
        return productBuilder()
            .price(new BigDecimal("6.00"))
            .sellerFeedbackPercentage("90.0")
            .sellerFeedbackScore(2)
            .shippingCost(new BigDecimal("35.00"))
            .shippingCostType("CALCULATED")
            .itemLocationCountry("US")
            .itemOriginDate(NOW.minus(DurationDays.days(700)))
            .build();
    }

    private ProductBuilder productBuilder() {
        return new ProductBuilder();
    }

    private static final class ProductBuilder {
        private BigDecimal price = new BigDecimal("64.00");
        private String sellerFeedbackPercentage = "99.0";
        private Integer sellerFeedbackScore = 1200;
        private String itemLocationCountry = "CA";
        private BigDecimal shippingCost = BigDecimal.ZERO;
        private String shippingCostType = "FIXED";
        private boolean topRatedBuyingExperience = true;
        private Instant itemOriginDate = NOW.minus(DurationDays.days(20));

        ProductBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        ProductBuilder sellerFeedbackPercentage(String sellerFeedbackPercentage) {
            this.sellerFeedbackPercentage = sellerFeedbackPercentage;
            return this;
        }

        ProductBuilder sellerFeedbackScore(Integer sellerFeedbackScore) {
            this.sellerFeedbackScore = sellerFeedbackScore;
            return this;
        }

        ProductBuilder itemLocationCountry(String itemLocationCountry) {
            this.itemLocationCountry = itemLocationCountry;
            return this;
        }

        ProductBuilder shippingCost(BigDecimal shippingCost) {
            this.shippingCost = shippingCost;
            return this;
        }

        ProductBuilder shippingCostType(String shippingCostType) {
            this.shippingCostType = shippingCostType;
            return this;
        }

        ProductBuilder topRatedBuyingExperience(boolean topRatedBuyingExperience) {
            this.topRatedBuyingExperience = topRatedBuyingExperience;
            return this;
        }

        ProductBuilder itemOriginDate(Instant itemOriginDate) {
            this.itemOriginDate = itemOriginDate;
            return this;
        }

        MarketplaceProductSignal build() {
            return new MarketplaceProductSignal(
                "test-item",
                "Test marketplace item",
                "Test category",
                null,
                null,
                "{\"source\":\"test\"}",
                price,
                "CAD",
                "New",
                "seller",
                sellerFeedbackPercentage,
                sellerFeedbackScore,
                itemLocationCountry,
                shippingCost,
                shippingCostType,
                List.of("FIXED_PRICE"),
                topRatedBuyingExperience,
                false,
                itemOriginDate,
                NOW
            );
        }
    }

    private static final class DurationDays {
        static java.time.Duration days(long days) {
            return java.time.Duration.ofDays(days);
        }
    }
}
