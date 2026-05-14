package com.trendradar.explanation;

import com.trendradar.domain.MarketplaceEvidence;
import com.trendradar.domain.ProductConcept;
import com.trendradar.domain.RiskSignal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OpportunityExplanationService {

    public String explain(
        ProductConcept productConcept,
        MarketplaceEvidence marketplaceEvidence,
        List<RiskSignal> risks
    ) {
        String primaryRisk = risks.isEmpty() ? "monitor supply quality" : risks.get(0).description();

        return "%s is showing %s. Median pricing around $%s suggests a testable entry point, while the main watchout is to %s."
            .formatted(
                productConcept.name(),
                marketplaceEvidence.demandSignal().toLowerCase(),
                marketplaceEvidence.medianPrice(),
                primaryRisk.toLowerCase()
            );
    }
}
