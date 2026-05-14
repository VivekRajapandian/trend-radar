package com.trendradar.scoring;

import com.trendradar.domain.RiskSignal;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketplaceProductSignal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OpportunityScoringService {

    private final Clock clock;

    public OpportunityScoringService() {
        this(Clock.systemUTC());
    }

    OpportunityScoringService(Clock clock) {
        this.clock = clock;
    }

    public OpportunityScore score(
        MarketSignalBatch signalBatch,
        MarketplaceProductSignal product,
        List<RiskSignal> risks
    ) {
        int marketplaceProofScore = scoreMarketplaceProof(signalBatch);
        int priceViabilityScore = scorePriceViability(signalBatch, product);
        int freshnessScore = scoreFreshness(product);
        int sellerQualityScore = scoreSellerQuality(product);
        int shippingRiskScore = scoreShippingRisk(product);
        int competitionRiskScore = scoreCompetitionRisk(signalBatch);

        int finalScore = clamp((int) Math.round(
            marketplaceProofScore * 0.25
                + priceViabilityScore * 0.20
                + freshnessScore * 0.15
                + sellerQualityScore * 0.15
                + shippingRiskScore * 0.10
                + competitionRiskScore * 0.15
        ));

        return new OpportunityScore(
            marketplaceProofScore,
            priceViabilityScore,
            freshnessScore,
            sellerQualityScore,
            shippingRiskScore,
            competitionRiskScore,
            finalScore,
            labelFor(finalScore)
        );
    }

    public double confidence(MarketplaceProductSignal product) {
        return scoreSellerQuality(product) / 100.0;
    }

    private int scoreMarketplaceProof(MarketSignalBatch signalBatch) {
        int totalMatches = Math.max(0, signalBatch.totalMatches());
        int activeListings = signalBatch.products().size();

        // Assumption: enough marketplace proof is good, but runaway listing volume should not dominate the final score.
        int totalScore = Math.min(70, totalMatches / 30);
        int sampleScore = Math.min(30, activeListings * 3);

        return clamp(totalScore + sampleScore);
    }

    private int scorePriceViability(MarketSignalBatch signalBatch, MarketplaceProductSignal product) {
        BigDecimal price = product.price();

        if (price == null || price.signum() <= 0) {
            return 30;
        }

        double value = price.doubleValue();
        int priceBandScore;

        // Assumption: early seller tests are most viable in a moderate price band with room for margin.
        if (value >= 20 && value <= 120) {
            priceBandScore = 90;
        } else if (value >= 10 && value < 20 || value > 120 && value <= 250) {
            priceBandScore = 70;
        } else if (value > 250 && value <= 450) {
            priceBandScore = 50;
        } else {
            priceBandScore = 35;
        }

        BigDecimal minPrice = minPrice(signalBatch.products());
        BigDecimal maxPrice = maxPrice(signalBatch.products());
        int spreadPenalty = 0;

        if (minPrice.signum() > 0 && maxPrice.compareTo(minPrice) > 0) {
            BigDecimal spreadRatio = maxPrice
                .subtract(minPrice)
                .divide(minPrice, 2, RoundingMode.HALF_UP);
            spreadPenalty = Math.min(25, spreadRatio.multiply(new BigDecimal("8")).intValue());
        }

        return clamp(priceBandScore - spreadPenalty);
    }

    private int scoreFreshness(MarketplaceProductSignal product) {
        Instant createdAt = product.itemOriginDate() != null ? product.itemOriginDate() : product.observedAt();

        if (createdAt == null) {
            return 40;
        }

        long ageDays = Math.max(0, Duration.between(createdAt, Instant.now(clock)).toDays());

        // Assumption: newer listings better represent current demand, but older listings still provide market context.
        if (ageDays <= 30) {
            return 100;
        } else if (ageDays <= 90) {
            return 80;
        } else if (ageDays <= 180) {
            return 60;
        } else if (ageDays <= 365) {
            return 45;
        }

        return 25;
    }

    private int scoreSellerQuality(MarketplaceProductSignal product) {
        int score = 35;

        if (product.sellerFeedbackPercentage() != null) {
            try {
                double feedbackPercentage = Double.parseDouble(product.sellerFeedbackPercentage());
                if (feedbackPercentage >= 99.5) {
                    score += 35;
                } else if (feedbackPercentage >= 98.0) {
                    score += 25;
                } else if (feedbackPercentage >= 95.0) {
                    score += 15;
                }
            } catch (NumberFormatException ignored) {
                score += 5;
            }
        }

        Integer feedbackScore = product.sellerFeedbackScore();
        if (feedbackScore != null) {
            if (feedbackScore >= 5000) {
                score += 20;
            } else if (feedbackScore >= 1000) {
                score += 15;
            } else if (feedbackScore >= 100) {
                score += 10;
            } else if (feedbackScore > 0) {
                score += 5;
            }
        }

        if (product.topRatedBuyingExperience()) {
            score += 10;
        }

        return clamp(score);
    }

    private int scoreShippingRisk(MarketplaceProductSignal product) {
        int score = 80;

        // Assumption: this score is higher when shipping risk is lower.
        if (product.shippingCost() == null) {
            score -= 10;
        } else if (product.shippingCost().compareTo(new BigDecimal("25")) > 0) {
            score -= 25;
        } else if (product.shippingCost().compareTo(new BigDecimal("10")) > 0) {
            score -= 10;
        }

        if ("CALCULATED".equalsIgnoreCase(product.shippingCostType())) {
            score -= 10;
        }

        if (product.itemLocationCountry() != null && !product.itemLocationCountry().equalsIgnoreCase("CA")) {
            score -= 12;
        }

        return clamp(score);
    }

    private int scoreCompetitionRisk(MarketSignalBatch signalBatch) {
        int totalMatches = Math.max(0, signalBatch.totalMatches());
        int activeListings = Math.max(0, signalBatch.products().size());

        // Assumption: this score is higher when competition risk is lower; very crowded markets become harder to enter.
        int score;
        if (totalMatches < 100) {
            score = 55;
        } else if (totalMatches <= 1500) {
            score = 85;
        } else if (totalMatches <= 7500) {
            score = 65;
        } else {
            score = 45;
        }

        if (activeListings >= 10) {
            score -= 5;
        }

        return clamp(score);
    }

    private String labelFor(int finalScore) {
        if (finalScore >= 80) {
            return "High";
        } else if (finalScore >= 60) {
            return "Promising";
        } else if (finalScore >= 40) {
            return "Watch";
        }

        return "Weak";
    }

    private BigDecimal minPrice(List<MarketplaceProductSignal> products) {
        return products.stream()
            .map(MarketplaceProductSignal::price)
            .filter(price -> price != null && price.signum() > 0)
            .min(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal maxPrice(List<MarketplaceProductSignal> products) {
        return products.stream()
            .map(MarketplaceProductSignal::price)
            .filter(price -> price != null && price.signum() > 0)
            .max(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
