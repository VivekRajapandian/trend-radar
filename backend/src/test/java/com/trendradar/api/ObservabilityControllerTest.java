package com.trendradar.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ObservabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void providerRunHistoryAndSystemStatusExposeOperationalState() throws Exception {
        mockMvc.perform(post("/api/opportunities/refresh?niche=anime_collectibles&region=CA"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/provider-runs?size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runs", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.runs[0].id").exists())
            .andExpect(jsonPath("$.runs[0].source").exists())
            .andExpect(jsonPath("$.runs[0].status").value("COMPLETED"))
            .andExpect(jsonPath("$.runs[0].recordsFetched").value(3))
            .andExpect(jsonPath("$.runs[0].opportunitiesGenerated").value(3))
            .andExpect(jsonPath("$.runs[0].scoringVersion").value("v1"))
            .andExpect(jsonPath("$.runs[0].createdAt").exists());

        mockMvc.perform(get("/api/system/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.backendStatus").value("OK"))
            .andExpect(jsonPath("$.dbConnectivity").value("OK"))
            .andExpect(jsonPath("$.latestProviderRun").exists())
            .andExpect(jsonPath("$.latestScoringRun").exists())
            .andExpect(jsonPath("$.totalOpportunitiesStored", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.totalSourceRecordsStored", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.activeProviders", hasSize(greaterThanOrEqualTo(1))));
    }
}
