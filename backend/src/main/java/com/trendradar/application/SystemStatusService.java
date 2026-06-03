package com.trendradar.application;

import com.trendradar.application.SystemStatusResponse.ProviderStatus;
import com.trendradar.application.SystemStatusResponse.ScoringRunSummary;
import com.trendradar.infrastructure.persistence.OpportunitySnapshotRepository;
import com.trendradar.infrastructure.persistence.ProviderRunRepository;
import com.trendradar.infrastructure.persistence.ScoringRunEntity;
import com.trendradar.infrastructure.persistence.ScoringRunRepository;
import com.trendradar.infrastructure.persistence.SeedTermRepository;
import com.trendradar.infrastructure.persistence.SourceRecordRepository;
import com.trendradar.provider.MarketSignalProvider;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemStatusService {

    private final DataSource dataSource;
    private final ProviderRunRepository providerRunRepository;
    private final ScoringRunRepository scoringRunRepository;
    private final OpportunitySnapshotRepository opportunitySnapshotRepository;
    private final SourceRecordRepository sourceRecordRepository;
    private final SeedTermRepository seedTermRepository;
    private final ProviderRunQueryService providerRunQueryService;
    private final List<MarketSignalProvider> marketSignalProviders;
    private final IngestionProperties ingestionProperties;

    public SystemStatusService(
        DataSource dataSource,
        ProviderRunRepository providerRunRepository,
        ScoringRunRepository scoringRunRepository,
        OpportunitySnapshotRepository opportunitySnapshotRepository,
        SourceRecordRepository sourceRecordRepository,
        SeedTermRepository seedTermRepository,
        ProviderRunQueryService providerRunQueryService,
        List<MarketSignalProvider> marketSignalProviders,
        IngestionProperties ingestionProperties
    ) {
        this.dataSource = dataSource;
        this.providerRunRepository = providerRunRepository;
        this.scoringRunRepository = scoringRunRepository;
        this.opportunitySnapshotRepository = opportunitySnapshotRepository;
        this.sourceRecordRepository = sourceRecordRepository;
        this.seedTermRepository = seedTermRepository;
        this.providerRunQueryService = providerRunQueryService;
        this.marketSignalProviders = marketSignalProviders;
        this.ingestionProperties = ingestionProperties;
    }

    @Transactional(readOnly = true)
    public SystemStatusResponse getStatus() {
        ProviderRunResponse latestProviderRun = providerRunRepository.findFirstByOrderByStartedAtDesc()
            .map(providerRunQueryService::toResponse)
            .orElse(null);

        ScoringRunSummary latestScoringRun = scoringRunRepository.findFirstByOrderByStartedAtDesc()
            .map(this::toScoringSummary)
            .orElse(null);

        return new SystemStatusResponse(
            "OK",
            dbConnectivity(),
            latestProviderRun,
            latestScoringRun,
            opportunitySnapshotRepository.count(),
            sourceRecordRepository.count(),
            marketSignalProviders.stream()
                .map(provider -> new ProviderStatus(provider.sourceType(), provider.isAvailable()))
                .toList(),
            ingestionProperties.enabled(),
            ingestionProperties.fixedRateMinutes(),
            seedTermRepository.countByEnabledTrue(),
            latestProviderRun == null ? null : latestProviderRun.startedAt(),
            Instant.now()
        );
    }

    private ScoringRunSummary toScoringSummary(ScoringRunEntity scoringRun) {
        return new ScoringRunSummary(
            scoringRun.getId(),
            scoringRun.getProviderRun() == null ? null : scoringRun.getProviderRun().getId(),
            scoringRun.getStatus(),
            scoringRun.getScoringVersion(),
            scoringRun.getStartedAt(),
            scoringRun.getCompletedAt(),
            durationMs(scoringRun.getStartedAt(), scoringRun.getCompletedAt()),
            scoringRun.getOpportunitiesScored(),
            scoringRun.getErrorMessage()
        );
    }

    private String dbConnectivity() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "OK" : "DEGRADED";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private Long durationMs(Instant startedAt, Instant completedAt) {
        if (startedAt == null || completedAt == null) {
            return null;
        }

        return Duration.between(startedAt, completedAt).toMillis();
    }
}
