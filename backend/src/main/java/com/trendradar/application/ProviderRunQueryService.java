package com.trendradar.application;

import com.trendradar.infrastructure.persistence.ProviderRunEntity;
import com.trendradar.infrastructure.persistence.ProviderRunRepository;
import com.trendradar.infrastructure.persistence.ScoringRunEntity;
import com.trendradar.infrastructure.persistence.ScoringRunRepository;
import java.time.Duration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderRunQueryService {

    private final ProviderRunRepository providerRunRepository;
    private final ScoringRunRepository scoringRunRepository;

    public ProviderRunQueryService(
        ProviderRunRepository providerRunRepository,
        ScoringRunRepository scoringRunRepository
    ) {
        this.providerRunRepository = providerRunRepository;
        this.scoringRunRepository = scoringRunRepository;
    }

    @Transactional(readOnly = true)
    public ProviderRunPageResponse findProviderRuns(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Page<ProviderRunEntity> runPage = providerRunRepository.findAllByOrderByStartedAtDesc(
            PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "startedAt"))
        );

        return new ProviderRunPageResponse(
            runPage.getContent().stream().map(this::toResponse).toList(),
            runPage.getNumber(),
            runPage.getSize(),
            runPage.getTotalElements(),
            runPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ProviderRunResponse findProviderRun(long id) {
        return providerRunRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new IllegalArgumentException("Provider run %d was not found".formatted(id)));
    }

    ProviderRunResponse toResponse(ProviderRunEntity providerRun) {
        ScoringRunEntity scoringRun = scoringRunRepository
            .findFirstByProviderRunIdOrderByStartedAtDesc(providerRun.getId())
            .orElse(null);

        return new ProviderRunResponse(
            providerRun.getId(),
            providerRun.getSourceType(),
            providerRun.getSourceType(),
            providerRun.getNicheCode(),
            providerRun.getRegion(),
            providerRun.getQuery(),
            providerRun.getStatus(),
            providerRun.getStartedAt(),
            providerRun.getCompletedAt(),
            durationMs(providerRun.getStartedAt(), providerRun.getCompletedAt()),
            providerRun.getRecordsFetched(),
            scoringRun == null ? 0 : scoringRun.getOpportunitiesScored(),
            scoringRun == null ? null : scoringRun.getScoringVersion(),
            providerRun.getErrorMessage(),
            providerRun.getCreatedAt()
        );
    }

    Long durationMs(java.time.Instant startedAt, java.time.Instant completedAt) {
        if (startedAt == null || completedAt == null) {
            return null;
        }

        return Duration.between(startedAt, completedAt).toMillis();
    }
}
