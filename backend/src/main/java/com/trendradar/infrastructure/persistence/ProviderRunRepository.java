package com.trendradar.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRunRepository extends JpaRepository<ProviderRunEntity, Long> {

    Page<ProviderRunEntity> findAllByOrderByStartedAtDesc(Pageable pageable);

    Optional<ProviderRunEntity> findFirstByOrderByStartedAtDesc();
}
