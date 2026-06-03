package com.trendradar.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeedTermRepository extends JpaRepository<SeedTermEntity, Long> {

    List<SeedTermEntity> findByOrderByPriorityDescCreatedAtAsc();

    List<SeedTermEntity> findByNicheAndRegionOrderByPriorityDescCreatedAtAsc(String niche, String region);

    List<SeedTermEntity> findByEnabledTrueOrderByPriorityDescCreatedAtAsc();

    List<SeedTermEntity> findByEnabledTrueAndNicheAndRegionOrderByPriorityDescCreatedAtAsc(String niche, String region);

    long countByEnabledTrue();
}
