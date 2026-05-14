package com.trendradar.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoringRunRepository extends JpaRepository<ScoringRunEntity, Long> {
}
