package com.lemerence.tender.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "review_reports")
public class ReviewReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private BidProject project;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String riskLevel;

    @Column(nullable = false, columnDefinition = "text")
    private String tenderText;

    @Column(nullable = false, columnDefinition = "text")
    private String bidText;

    @Column(nullable = false, columnDefinition = "text")
    private String reportJson;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BidProject getProject() { return project; }
    public void setProject(BidProject project) { this.project = project; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getTenderText() { return tenderText; }
    public void setTenderText(String tenderText) { this.tenderText = tenderText; }
    public String getBidText() { return bidText; }
    public void setBidText(String bidText) { this.bidText = bidText; }
    public String getReportJson() { return reportJson; }
    public void setReportJson(String reportJson) { this.reportJson = reportJson; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
