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
import java.time.Instant;

@Entity
@Table(name = "scoring_run")
public class ScoringRunEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_run_id", nullable = false)
    private ProviderRunEntity providerRun;

    @Column(name = "scoring_version", nullable = false)
    private String scoringVersion;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "opportunities_scored", nullable = false)
    private int opportunitiesScored;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public ProviderRunEntity getProviderRun() {
        return providerRun;
    }

    public String getScoringVersion() {
        return scoringVersion;
    }

    public String getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public int getOpportunitiesScored() {
        return opportunitiesScored;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setProviderRun(ProviderRunEntity providerRun) {
        this.providerRun = providerRun;
    }

    public void setScoringVersion(String scoringVersion) {
        this.scoringVersion = scoringVersion;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public void setOpportunitiesScored(int opportunitiesScored) {
        this.opportunitiesScored = opportunitiesScored;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
