package com.trendradar.api;

import com.trendradar.application.OpportunityQueryService;
import com.trendradar.domain.OpportunitySnapshot;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpportunityController {

    private final OpportunityQueryService opportunityQueryService;

    public OpportunityController(OpportunityQueryService opportunityQueryService) {
        this.opportunityQueryService = opportunityQueryService;
    }

    @GetMapping("/api/opportunities")
    public List<OpportunitySnapshot> getOpportunities(
        @RequestParam(defaultValue = "anime_collectibles") String niche,
        @RequestParam(defaultValue = "CA") String region
    ) {
        return opportunityQueryService.findOpportunities(niche, region);
    }
}
