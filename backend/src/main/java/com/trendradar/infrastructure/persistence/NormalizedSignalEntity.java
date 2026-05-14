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
@Table(name = "normalized_signal")
public class NormalizedSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_run_id", nullable = false)
    private ProviderRunEntity providerRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_record_id")
    private SourceRecordEntity sourceRecord;

    @Column(name = "product_concept_id", nullable = false)
    private String productConceptId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "niche_code", nullable = false)
    private String nicheCode;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "seller_feedback_percentage")
    private String sellerFeedbackPercentage;

    @Column(name = "seller_feedback_score")
    private Integer sellerFeedbackScore;

    @Column(name = "item_location_country")
    private String itemLocationCountry;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "shipping_cost_type")
    private String shippingCostType;

    @Column(name = "condition")
    private String condition;

    @Column(name = "buying_options")
    private String buyingOptions;

    @Column(name = "normalized_at", nullable = false)
    private Instant normalizedAt;

    public Long getId() {
        return id;
    }

    public void setProviderRun(ProviderRunEntity providerRun) {
        this.providerRun = providerRun;
    }

    public void setSourceRecord(SourceRecordEntity sourceRecord) {
        this.sourceRecord = sourceRecord;
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

    public void setNicheCode(String nicheCode) {
        this.nicheCode = nicheCode;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSellerFeedbackPercentage(String sellerFeedbackPercentage) {
        this.sellerFeedbackPercentage = sellerFeedbackPercentage;
    }

    public void setSellerFeedbackScore(Integer sellerFeedbackScore) {
        this.sellerFeedbackScore = sellerFeedbackScore;
    }

    public void setItemLocationCountry(String itemLocationCountry) {
        this.itemLocationCountry = itemLocationCountry;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public void setShippingCostType(String shippingCostType) {
        this.shippingCostType = shippingCostType;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setBuyingOptions(String buyingOptions) {
        this.buyingOptions = buyingOptions;
    }

    public void setNormalizedAt(Instant normalizedAt) {
        this.normalizedAt = normalizedAt;
    }
}
