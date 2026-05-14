package com.trendradar.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NormalizedSignalRepository extends JpaRepository<NormalizedSignalEntity, Long> {
}
