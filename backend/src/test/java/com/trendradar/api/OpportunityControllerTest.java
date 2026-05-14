package com.trendradar.api;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOpportunitiesReturnsNormalizedSnapshots() throws Exception {
        mockMvc.perform(get("/api/opportunities?niche=anime_collectibles&region=CA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].productConcept.name", not(blankOrNullString())))
            .andExpect(jsonPath("$[0].niche.code").value("anime_collectibles"))
            .andExpect(jsonPath("$[0].region.code").value("CA"))
            .andExpect(jsonPath("$[0].score", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$[0].scoreLabel").exists())
            .andExpect(jsonPath("$[0].marketplaceProofScore").exists())
            .andExpect(jsonPath("$[0].priceViabilityScore").exists())
            .andExpect(jsonPath("$[0].freshnessScore").exists())
            .andExpect(jsonPath("$[0].sellerQualityScore").exists())
            .andExpect(jsonPath("$[0].shippingRiskScore").exists())
            .andExpect(jsonPath("$[0].competitionRiskScore").exists())
            .andExpect(jsonPath("$[0].finalScore").exists())
            .andExpect(jsonPath("$[0].marketplaceEvidence").exists())
            .andExpect(jsonPath("$[0].marketplaceEvidence.minPrice").exists())
            .andExpect(jsonPath("$[0].marketplaceEvidence.maxPrice").exists())
            .andExpect(jsonPath("$[0].sourceEvidence").isArray())
            .andExpect(jsonPath("$[0].sourceEvidence[0].sourceType").value("marketplace_mock"))
            .andExpect(jsonPath("$[0].risks").isArray())
            .andExpect(jsonPath("$[0].explanation").exists())
            .andExpect(jsonPath("$[0].generatedAt").exists());
    }
}
