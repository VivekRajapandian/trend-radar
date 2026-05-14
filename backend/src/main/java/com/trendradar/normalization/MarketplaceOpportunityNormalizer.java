package com.trendradar.normalization;

import com.trendradar.domain.MarketplaceEvidence;
import com.trendradar.domain.Niche;
import com.trendradar.domain.OpportunitySnapshot;
import com.trendradar.domain.ProductConcept;
import com.trendradar.domain.Region;
import com.trendradar.domain.RiskSignal;
import com.trendradar.domain.SourceEvidence;
import com.trendradar.explanation.OpportunityExplanationService;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketplaceProductSignal;
import com.trendradar.scoring.OpportunityScore;
import com.trendradar.scoring.OpportunityScoringService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceOpportunityNormalizer implements OpportunityNormalizer {

    private final OpportunityScoringService scoringService;
    private final OpportunityExplanationService explanationService;

    public MarketplaceOpportunityNormalizer(
        OpportunityScoringService scoringService,
        OpportunityExplanationService explanationService
    ) {
        this.scoringService = scoringService;
        this.explanationService = explanationService;
    }

    @Override
    public List<OpportunitySnapshot> normalize(MarketSignalBatch signalBatch, Niche niche, Region region) {
        return signalBatch.products().stream()
            .map(product -> toSnapshot(signalBatch, product, niche, region))
            .sorted(Comparator.comparingInt(OpportunitySnapshot::score).reversed())
            .limit(10)
            .toList();
    }

    private OpportunitySnapshot toSnapshot(
        MarketSignalBatch signalBatch,
        MarketplaceProductSignal product,
        Niche niche,
        Region region
    ) {
        MarketplaceEvidence marketplaceEvidence = new MarketplaceEvidence(
            signalBatch.totalMatches(),
            signalBatch.products().size(),
            product.price() == null ? BigDecimal.ZERO : product.price(),
            buildDemandSignal(signalBatch, product)
        );
        List<RiskSignal> risks = buildRisks(product, niche);
        OpportunityScore score = scoringService.score(signalBatch, product, risks);
        ProductConcept productConcept = new ProductConcept(
            product.id(),
            toProductConceptName(product.title()),
            product.categoryName()
        );
        Instant generatedAt = Instant.now();

        return new OpportunitySnapshot(
            productConcept,
            niche,
            region,
            score.value(),
            score.label(),
            marketplaceEvidence,
            List.of(
                new SourceEvidence(
                    signalBatch.sourceType(),
                    product.title(),
                    scoringService.confidence(product),
                    product.observedAt()
                )
            ),
            risks,
            explanationService.explain(productConcept, marketplaceEvidence, risks),
            generatedAt
        );
    }

    private String buildDemandSignal(MarketSignalBatch signalBatch, MarketplaceProductSignal product) {
        String source = signalBatch.sourceType().equals("ebay_browse")
            ? "Live eBay Browse result"
            : "Mock marketplace signal";

        return "%s with %d total matches for query \"%s\"; seller feedback %s%%"
            .formatted(source, signalBatch.totalMatches(), signalBatch.query(), product.sellerFeedbackPercentage());
    }

    private List<RiskSignal> buildRisks(MarketplaceProductSignal product, Niche niche) {
        List<RiskSignal> risks = new java.util.ArrayList<>();

        if (niche.code().equals("anime_collectibles")) {
            risks.add(new RiskSignal("licensed_ip", "MEDIUM", "Character merchandise may require careful sourcing"));
        }

        if (product.condition() != null && product.condition().equalsIgnoreCase("Used")) {
            risks.add(new RiskSignal("condition_variance", "MEDIUM", "Used collectible condition can vary materially"));
        }

        if (product.itemLocationCountry() != null && !product.itemLocationCountry().equalsIgnoreCase("CA")) {
            risks.add(new RiskSignal("cross_border_supply", "LOW", "Cross-border shipping may affect delivery time and margin"));
        }

        if (!product.topRatedBuyingExperience()) {
            risks.add(new RiskSignal("seller_quality", "LOW", "Listing is not marked as a top-rated buying experience"));
        }

        return risks;
    }

    private String toProductConceptName(String title) {
        String normalized = title == null || title.isBlank() ? "Marketplace product concept" : title.trim();
        String[] words = normalized.split("\\s+");

        if (words.length <= 5) {
            return normalized;
        }

        return String.join(" ", java.util.Arrays.copyOfRange(words, 0, 5));
    }
}
