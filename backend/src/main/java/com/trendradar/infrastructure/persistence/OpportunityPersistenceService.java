package com.trendradar.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendradar.domain.MarketplaceEvidence;
import com.trendradar.domain.Niche;
import com.trendradar.domain.OpportunitySnapshot;
import com.trendradar.domain.ProductConcept;
import com.trendradar.domain.Region;
import com.trendradar.domain.RiskSignal;
import com.trendradar.domain.SourceEvidence;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketplaceProductSignal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpportunityPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpportunityPersistenceService.class);

    private static final TypeReference<List<SourceEvidence>> SOURCE_EVIDENCE_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<RiskSignal>> RISK_SIGNAL_LIST = new TypeReference<>() {
    };

    private final ProviderRunRepository providerRunRepository;
    private final SourceRecordRepository sourceRecordRepository;
    private final NormalizedSignalRepository normalizedSignalRepository;
    private final ScoringRunRepository scoringRunRepository;
    private final OpportunitySnapshotRepository opportunitySnapshotRepository;
    private final ObjectMapper objectMapper;

    public OpportunityPersistenceService(
        ProviderRunRepository providerRunRepository,
        SourceRecordRepository sourceRecordRepository,
        NormalizedSignalRepository normalizedSignalRepository,
        ScoringRunRepository scoringRunRepository,
        OpportunitySnapshotRepository opportunitySnapshotRepository,
        ObjectMapper objectMapper
    ) {
        this.providerRunRepository = providerRunRepository;
        this.sourceRecordRepository = sourceRecordRepository;
        this.normalizedSignalRepository = normalizedSignalRepository;
        this.scoringRunRepository = scoringRunRepository;
        this.opportunitySnapshotRepository = opportunitySnapshotRepository;
        this.objectMapper = objectMapper;
    }

    public List<OpportunitySnapshot> findLatest(Niche niche, Region region) {
        return opportunitySnapshotRepository.findFirstByNicheCodeAndRegionCodeOrderByGeneratedAtDesc(
                niche.code(),
                region.code()
            )
            .map(latest -> opportunitySnapshotRepository.findByScoringRunIdOrderByFinalScoreDesc(
                latest.getScoringRun().getId()
            ))
            .orElse(List.of())
            .stream()
            .map(this::toDomain)
            .toList();
    }

    public ProviderRunEntity startProviderRun(String sourceType, String query, Niche niche, Region region) {
        ProviderRunEntity providerRun = new ProviderRunEntity();
        providerRun.setSourceType(sourceType);
        providerRun.setQuery(query);
        providerRun.setNicheCode(niche.code());
        providerRun.setRegion(region.code());
        providerRun.setStatus("RUNNING");
        providerRun.setStartedAt(Instant.now());
        providerRun.setRecordsFetched(0);

        ProviderRunEntity savedProviderRun = providerRunRepository.save(providerRun);
        LOGGER.info("Started provider run {} for source {} and query {}", savedProviderRun.getId(), sourceType, query);

        return savedProviderRun;
    }

    public void completeProviderRun(ProviderRunEntity providerRun, MarketSignalBatch signalBatch) {
        providerRun.setSourceType(signalBatch.sourceType());
        providerRun.setQuery(signalBatch.query());
        providerRun.setStatus("COMPLETED");
        providerRun.setCompletedAt(Instant.now());
        providerRun.setRecordsFetched(signalBatch.products().size());
        providerRunRepository.save(providerRun);
        LOGGER.info("Completed provider run {} with {} records", providerRun.getId(), signalBatch.products().size());
    }

    public void failProviderRun(ProviderRunEntity providerRun, RuntimeException exception) {
        providerRun.setStatus("FAILED");
        providerRun.setCompletedAt(Instant.now());
        providerRun.setErrorMessage(exception.getMessage());
        providerRunRepository.save(providerRun);
        LOGGER.warn("Provider run {} failed: {}", providerRun.getId(), exception.getMessage());
    }

    public List<SourceRecordEntity> saveSourceRecords(ProviderRunEntity providerRun, MarketSignalBatch signalBatch) {
        List<SourceRecordEntity> records = signalBatch.products().stream()
            .map(product -> toSourceRecord(providerRun, signalBatch, product))
            .toList();

        List<SourceRecordEntity> savedRecords = sourceRecordRepository.saveAll(records);
        LOGGER.info("Persisted {} source records for provider run {}", savedRecords.size(), providerRun.getId());

        return savedRecords;
    }

    public void saveNormalizedSignals(
        ProviderRunEntity providerRun,
        List<SourceRecordEntity> sourceRecords,
        MarketSignalBatch signalBatch,
        Niche niche,
        Region region
    ) {
        List<NormalizedSignalEntity> normalizedSignals = new ArrayList<>();
        List<MarketplaceProductSignal> products = signalBatch.products();

        for (int index = 0; index < products.size(); index++) {
            normalizedSignals.add(toNormalizedSignal(
                providerRun,
                sourceRecords.size() > index ? sourceRecords.get(index) : null,
                products.get(index),
                niche,
                region
            ));
        }

        normalizedSignalRepository.saveAll(normalizedSignals);
        LOGGER.info("Persisted {} normalized signals for provider run {}", normalizedSignals.size(), providerRun.getId());
    }

    public ScoringRunEntity startScoringRun(ProviderRunEntity providerRun) {
        ScoringRunEntity scoringRun = new ScoringRunEntity();
        scoringRun.setProviderRun(providerRun);
        scoringRun.setScoringVersion("v1");
        scoringRun.setStatus("RUNNING");
        scoringRun.setStartedAt(Instant.now());
        scoringRun.setOpportunitiesScored(0);

        ScoringRunEntity savedScoringRun = scoringRunRepository.save(scoringRun);
        LOGGER.info("Started scoring run {} for provider run {}", savedScoringRun.getId(), providerRun.getId());

        return savedScoringRun;
    }

    public void completeScoringRun(ScoringRunEntity scoringRun, int opportunitiesScored) {
        scoringRun.setStatus("COMPLETED");
        scoringRun.setCompletedAt(Instant.now());
        scoringRun.setOpportunitiesScored(opportunitiesScored);
        scoringRunRepository.save(scoringRun);
        LOGGER.info("Completed scoring run {} with {} opportunities", scoringRun.getId(), opportunitiesScored);
    }

    public void failScoringRun(ScoringRunEntity scoringRun, RuntimeException exception) {
        scoringRun.setStatus("FAILED");
        scoringRun.setCompletedAt(Instant.now());
        scoringRun.setErrorMessage(exception.getMessage());
        scoringRunRepository.save(scoringRun);
        LOGGER.warn("Scoring run {} failed: {}", scoringRun.getId(), exception.getMessage());
    }

    public void saveOpportunitySnapshots(
        ProviderRunEntity providerRun,
        ScoringRunEntity scoringRun,
        List<OpportunitySnapshot> snapshots
    ) {
        opportunitySnapshotRepository.saveAll(
            snapshots.stream()
                .map(snapshot -> toOpportunitySnapshotEntity(providerRun, scoringRun, snapshot))
                .toList()
        );
        LOGGER.info("Persisted {} opportunity snapshots for scoring run {}", snapshots.size(), scoringRun.getId());
    }

    private SourceRecordEntity toSourceRecord(
        ProviderRunEntity providerRun,
        MarketSignalBatch signalBatch,
        MarketplaceProductSignal product
    ) {
        SourceRecordEntity sourceRecord = new SourceRecordEntity();
        sourceRecord.setProviderRun(providerRun);
        sourceRecord.setSourceType(signalBatch.sourceType());
        sourceRecord.setExternalId(product.id());
        sourceRecord.setTitle(product.title());
        sourceRecord.setRawJson(product.rawJson() == null || product.rawJson().isBlank() ? "{}" : product.rawJson());
        sourceRecord.setFetchedAt(Instant.now());

        return sourceRecord;
    }

    private NormalizedSignalEntity toNormalizedSignal(
        ProviderRunEntity providerRun,
        SourceRecordEntity sourceRecord,
        MarketplaceProductSignal product,
        Niche niche,
        Region region
    ) {
        NormalizedSignalEntity normalizedSignal = new NormalizedSignalEntity();
        normalizedSignal.setProviderRun(providerRun);
        normalizedSignal.setSourceRecord(sourceRecord);
        normalizedSignal.setProductConceptId(product.id());
        normalizedSignal.setProductName(product.title());
        normalizedSignal.setCategoryName(product.categoryName());
        normalizedSignal.setNicheCode(niche.code());
        normalizedSignal.setRegion(region.code());
        normalizedSignal.setPrice(product.price());
        normalizedSignal.setCurrency(product.currency());
        normalizedSignal.setSellerFeedbackPercentage(product.sellerFeedbackPercentage());
        normalizedSignal.setSellerFeedbackScore(product.sellerFeedbackScore());
        normalizedSignal.setItemLocationCountry(product.itemLocationCountry());
        normalizedSignal.setShippingCost(product.shippingCost());
        normalizedSignal.setShippingCostType(product.shippingCostType());
        normalizedSignal.setCondition(product.condition());
        normalizedSignal.setBuyingOptions(product.buyingOptions() == null ? "" : String.join(",", product.buyingOptions()));
        normalizedSignal.setNormalizedAt(Instant.now());

        return normalizedSignal;
    }

    private OpportunitySnapshotEntity toOpportunitySnapshotEntity(
        ProviderRunEntity providerRun,
        ScoringRunEntity scoringRun,
        OpportunitySnapshot snapshot
    ) {
        OpportunitySnapshotEntity entity = new OpportunitySnapshotEntity();
        entity.setProviderRun(providerRun);
        entity.setScoringRun(scoringRun);
        entity.setProductConceptId(snapshot.productConcept().id());
        entity.setProductName(snapshot.productConcept().name());
        entity.setCategoryName(snapshot.productConcept().category());
        entity.setImageUrl(snapshot.productConcept().imageUrl());
        entity.setNicheCode(snapshot.niche().code());
        entity.setNicheDisplayName(snapshot.niche().displayName());
        entity.setRegionCode(snapshot.region().code());
        entity.setRegionDisplayName(snapshot.region().displayName());
        entity.setScore(snapshot.score());
        entity.setScoreLabel(snapshot.scoreLabel());
        entity.setMarketplaceProofScore(snapshot.marketplaceProofScore());
        entity.setPriceViabilityScore(snapshot.priceViabilityScore());
        entity.setFreshnessScore(snapshot.freshnessScore());
        entity.setSellerQualityScore(snapshot.sellerQualityScore());
        entity.setShippingRiskScore(snapshot.shippingRiskScore());
        entity.setCompetitionRiskScore(snapshot.competitionRiskScore());
        entity.setFinalScore(snapshot.finalScore());
        entity.setEstimatedSoldCount(snapshot.marketplaceEvidence().estimatedSoldCount());
        entity.setActiveListings(snapshot.marketplaceEvidence().activeListings());
        entity.setMedianPrice(snapshot.marketplaceEvidence().medianPrice());
        entity.setMinPrice(snapshot.marketplaceEvidence().minPrice());
        entity.setMaxPrice(snapshot.marketplaceEvidence().maxPrice());
        entity.setDemandSignal(snapshot.marketplaceEvidence().demandSignal());
        entity.setSourceEvidenceJson(toJson(snapshot.sourceEvidence()));
        entity.setRisksJson(toJson(snapshot.risks()));
        entity.setExplanation(snapshot.explanation());
        entity.setGeneratedAt(snapshot.generatedAt());

        return entity;
    }

    private OpportunitySnapshot toDomain(OpportunitySnapshotEntity entity) {
        return new OpportunitySnapshot(
            new ProductConcept(
                entity.getProductConceptId(),
                entity.getProductName(),
                entity.getCategoryName(),
                entity.getImageUrl()
            ),
            new Niche(entity.getNicheCode(), entity.getNicheDisplayName()),
            new Region(entity.getRegionCode(), entity.getRegionDisplayName()),
            entity.getScore(),
            entity.getScoreLabel(),
            entity.getMarketplaceProofScore(),
            entity.getPriceViabilityScore(),
            entity.getFreshnessScore(),
            entity.getSellerQualityScore(),
            entity.getShippingRiskScore(),
            entity.getCompetitionRiskScore(),
            entity.getFinalScore(),
            new MarketplaceEvidence(
                entity.getEstimatedSoldCount(),
                entity.getActiveListings(),
                entity.getMedianPrice(),
                entity.getMinPrice(),
                entity.getMaxPrice(),
                entity.getDemandSignal()
            ),
            fromJson(entity.getSourceEvidenceJson(), SOURCE_EVIDENCE_LIST),
            fromJson(entity.getRisksJson(), RISK_SIGNAL_LIST),
            entity.getExplanation(),
            entity.getGeneratedAt()
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize persisted opportunity payload", exception);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize persisted opportunity payload", exception);
        }
    }
}
