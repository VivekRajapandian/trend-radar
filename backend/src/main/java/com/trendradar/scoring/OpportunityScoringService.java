package com.trendradar.scoring;

import com.trendradar.domain.RiskSignal;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketplaceProductSignal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OpportunityScoringService {

    public OpportunityScore score(int mockedValue) {
        String label;

        if (mockedValue >= 85) {
            label = "High";
        } else if (mockedValue >= 75) {
            label = "Promising";
        } else {
            label = "Watch";
        }

        return new OpportunityScore(mockedValue, label);
    }

    public OpportunityScore score(
        MarketSignalBatch signalBatch,
        MarketplaceProductSignal product,
        List<RiskSignal> risks
    ) {
        int score = 55;

        score += Math.min(18, signalBatch.totalMatches() / 500);

        if (product.topRatedBuyingExperience()) {
            score += 10;
        }

        if (product.priorityListing()) {
            score += 4;
        }

        if (product.price() != null && product.price().signum() > 0) {
            score += 8;
        }

        score += (int) Math.round(confidence(product) * 8);
        score -= Math.min(10, risks.size() * 2);

        return score(Math.max(1, Math.min(100, score)));
    }

    public double confidence(MarketplaceProductSignal product) {
        double confidence = 0.58;

        if (product.sellerFeedbackPercentage() != null) {
            try {
                confidence += Double.parseDouble(product.sellerFeedbackPercentage()) >= 99.0 ? 0.15 : 0.08;
            } catch (NumberFormatException ignored) {
                confidence += 0.04;
            }
        }

        if (product.topRatedBuyingExperience()) {
            confidence += 0.12;
        }

        if (product.price() != null && product.price().signum() > 0) {
            confidence += 0.08;
        }

        return Math.min(0.95, confidence);
    }
}
