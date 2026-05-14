package com.trendradar.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunitySnapshotRepository extends JpaRepository<OpportunitySnapshotEntity, Long> {

    Optional<OpportunitySnapshotEntity> findFirstByNicheCodeAndRegionCodeOrderByGeneratedAtDesc(
        String nicheCode,
        String regionCode
    );

    List<OpportunitySnapshotEntity> findByScoringRunIdOrderByFinalScoreDesc(Long scoringRunId);
}
