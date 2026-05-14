package com.trendradar.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoringRunRepository extends JpaRepository<ScoringRunEntity, Long> {

    Optional<ScoringRunEntity> findFirstByProviderRunIdOrderByStartedAtDesc(Long providerRunId);

    Optional<ScoringRunEntity> findFirstByOrderByStartedAtDesc();
}
