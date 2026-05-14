package com.trendradar.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendradar.application.OpportunityQueryService;
import com.trendradar.domain.OpportunitySnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpportunityPersistenceServiceTest {

    @Autowired
    private OpportunityQueryService opportunityQueryService;

    @Autowired
    private ProviderRunRepository providerRunRepository;

    @Autowired
    private SourceRecordRepository sourceRecordRepository;

    @Autowired
    private NormalizedSignalRepository normalizedSignalRepository;

    @Autowired
    private ScoringRunRepository scoringRunRepository;

    @Autowired
    private OpportunitySnapshotRepository opportunitySnapshotRepository;

    @Test
    void refreshPersistsProviderRecordsSignalsScoringRunAndSnapshots() {
        List<OpportunitySnapshot> opportunities = opportunityQueryService.refreshOpportunities(
            "anime_collectibles",
            "CA"
        );

        assertThat(opportunities).hasSize(3);
        assertThat(providerRunRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(sourceRecordRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(normalizedSignalRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(scoringRunRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(opportunitySnapshotRepository.count()).isGreaterThanOrEqualTo(3);

        opportunityQueryService.refreshOpportunities("anime_collectibles", "CA");
        assertThat(opportunityQueryService.findOpportunities("anime_collectibles", "CA")).hasSize(3);
    }
}
