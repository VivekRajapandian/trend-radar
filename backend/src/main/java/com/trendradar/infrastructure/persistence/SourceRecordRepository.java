package com.trendradar.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRecordRepository extends JpaRepository<SourceRecordEntity, Long> {
}
