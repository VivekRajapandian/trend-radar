package com.trendradar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "opportunity_snapshot")
public class OpportunitySnapshotEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scoring_run_id", nullable = false)
    private ScoringRunEntity scoringRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_run_id", nullable = false)
    private ProviderRunEntity providerRun;

    @Column(name = "product_concept_id", nullable = false)
    private String productConceptId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "image_url", length = 1200)
    private String imageUrl;

    @Column(name = "niche_code", nullable = false)
    private String nicheCode;

    @Column(name = "niche_display_name", nullable = false)
    private String nicheDisplayName;

    @Column(name = "region_code", nullable = false)
    private String regionCode;

    @Column(name = "region_display_name", nullable = false)
    private String regionDisplayName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "score_label", nullable = false)
    private String scoreLabel;

    @Column(name = "marketplace_proof_score", nullable = false)
    private int marketplaceProofScore;

    @Column(name = "price_viability_score", nullable = false)
    private int priceViabilityScore;

    @Column(name = "freshness_score", nullable = false)
    private int freshnessScore;

    @Column(name = "seller_quality_score", nullable = false)
    private int sellerQualityScore;

    @Column(name = "shipping_risk_score", nullable = false)
    private int shippingRiskScore;

    @Column(name = "competition_risk_score", nullable = false)
    private int competitionRiskScore;

    @Column(name = "final_score", nullable = false)
    private int finalScore;

    @Column(name = "estimated_sold_count", nullable = false)
    private int estimatedSoldCount;

    @Column(name = "active_listings", nullable = false)
    private int activeListings;

    @Column(name = "median_price")
    private BigDecimal medianPrice;

    @Column(name = "min_price")
    private BigDecimal minPrice;

    @Column(name = "max_price")
    private BigDecimal maxPrice;

    @Column(name = "demand_signal", nullable = false, columnDefinition = "TEXT")
    private String demandSignal;

    @Column(name = "source_evidence_json", nullable = false, columnDefinition = "TEXT")
    private String sourceEvidenceJson;

    @Column(name = "risks_json", nullable = false, columnDefinition = "TEXT")
    private String risksJson;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public Long getId() {
        return id;
    }

    public ScoringRunEntity getScoringRun() {
        return scoringRun;
    }

    public ProviderRunEntity getProviderRun() {
        return providerRun;
    }

    public String getProductConceptId() {
        return productConceptId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getNicheCode() {
        return nicheCode;
    }

    public String getNicheDisplayName() {
        return nicheDisplayName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getRegionDisplayName() {
        return regionDisplayName;
    }

    public int getScore() {
        return score;
    }

    public String getScoreLabel() {
        return scoreLabel;
    }

    public int getMarketplaceProofScore() {
        return marketplaceProofScore;
    }

    public int getPriceViabilityScore() {
        return priceViabilityScore;
    }

    public int getFreshnessScore() {
        return freshnessScore;
    }

    public int getSellerQualityScore() {
        return sellerQualityScore;
    }

    public int getShippingRiskScore() {
        return shippingRiskScore;
    }

    public int getCompetitionRiskScore() {
        return competitionRiskScore;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public int getEstimatedSoldCount() {
        return estimatedSoldCount;
    }

    public int getActiveListings() {
        return activeListings;
    }

    public BigDecimal getMedianPrice() {
        return medianPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public String getDemandSignal() {
        return demandSignal;
    }

    public String getSourceEvidenceJson() {
        return sourceEvidenceJson;
    }

    public String getRisksJson() {
        return risksJson;
    }

    public String getExplanation() {
        return explanation;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setScoringRun(ScoringRunEntity scoringRun) {
        this.scoringRun = scoringRun;
    }

    public void setProviderRun(ProviderRunEntity providerRun) {
        this.providerRun = providerRun;
    }

    public void setProductConceptId(String productConceptId) {
        this.productConceptId = productConceptId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setNicheCode(String nicheCode) {
        this.nicheCode = nicheCode;
    }

    public void setNicheDisplayName(String nicheDisplayName) {
        this.nicheDisplayName = nicheDisplayName;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void setRegionDisplayName(String regionDisplayName) {
        this.regionDisplayName = regionDisplayName;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setScoreLabel(String scoreLabel) {
        this.scoreLabel = scoreLabel;
    }

    public void setMarketplaceProofScore(int marketplaceProofScore) {
        this.marketplaceProofScore = marketplaceProofScore;
    }

    public void setPriceViabilityScore(int priceViabilityScore) {
        this.priceViabilityScore = priceViabilityScore;
    }

    public void setFreshnessScore(int freshnessScore) {
        this.freshnessScore = freshnessScore;
    }

    public void setSellerQualityScore(int sellerQualityScore) {
        this.sellerQualityScore = sellerQualityScore;
    }

    public void setShippingRiskScore(int shippingRiskScore) {
        this.shippingRiskScore = shippingRiskScore;
    }

    public void setCompetitionRiskScore(int competitionRiskScore) {
        this.competitionRiskScore = competitionRiskScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public void setEstimatedSoldCount(int estimatedSoldCount) {
        this.estimatedSoldCount = estimatedSoldCount;
    }

    public void setActiveListings(int activeListings) {
        this.activeListings = activeListings;
    }

    public void setMedianPrice(BigDecimal medianPrice) {
        this.medianPrice = medianPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setDemandSignal(String demandSignal) {
        this.demandSignal = demandSignal;
    }

    public void setSourceEvidenceJson(String sourceEvidenceJson) {
        this.sourceEvidenceJson = sourceEvidenceJson;
    }

    public void setRisksJson(String risksJson) {
        this.risksJson = risksJson;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
