package com.trendradar.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seedTermApiSupportsListingCreatingUpdatingAndDeletingTerms() throws Exception {
        mockMvc.perform(get("/api/seed-terms?niche=anime_collectibles&region=CA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$[0].searchTerm").exists())
            .andExpect(jsonPath("$[0].sourceType").value("ebay_browse"));

        String createdPayload = """
            {
              "niche": "anime_collectibles",
              "region": "CA",
              "searchTerm": "anime enamel pin",
              "enabled": true,
              "priority": 55
            }
            """;

        String createdJson = mockMvc.perform(post("/api/seed-terms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createdPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.searchTerm").value("anime enamel pin"))
            .andExpect(jsonPath("$.sourceType").value("ebay_browse"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Number id = com.jayway.jsonpath.JsonPath.read(createdJson, "$.id");

        mockMvc.perform(patch("/api/seed-terms/{id}", id.longValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":false,\"priority\":25}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false))
            .andExpect(jsonPath("$.priority").value(25));

        mockMvc.perform(delete("/api/seed-terms/{id}", id.longValue()))
            .andExpect(status().isNoContent());
    }

    @Test
    void manualIngestionRunsEnabledSeedTermsAndStatusExposesSchedulerFields() throws Exception {
        mockMvc.perform(post("/api/ingestion/run?niche=anime_collectibles&region=CA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSeedTerms", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.successfulRuns", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.failedRuns").value(0))
            .andExpect(jsonPath("$.totalRecordsFetched", greaterThanOrEqualTo(6)))
            .andExpect(jsonPath("$.opportunitiesGenerated", greaterThanOrEqualTo(6)));

        mockMvc.perform(get("/api/system/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.schedulerEnabled").value(false))
            .andExpect(jsonPath("$.schedulerFixedRateMinutes").value(360))
            .andExpect(jsonPath("$.enabledSeedTermCount", greaterThanOrEqualTo(4)))
            .andExpect(jsonPath("$.latestIngestionRunAt").exists());
    }
}
