package com.servicelens.platform.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "detection_rules")
public class DetectionRule {

    @Id
    private UUID id;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "target_service", length = 256)
    private String targetService;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_key", nullable = false, length = 64)
    private MetricKey metricKey;

    @Column(nullable = false)
    private double threshold;

    @Column(name = "window_minutes", nullable = false)
    private int windowMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ComparisonOperator comparison;

    @Column(name = "dedupe_fingerprint_template", length = 512)
    private String dedupeFingerprintTemplate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DetectionRule() {
    }

    public DetectionRule(
            UUID id,
            String name,
            boolean enabled,
            String targetService,
            MetricKey metricKey,
            double threshold,
            int windowMinutes,
            ComparisonOperator comparison,
            String dedupeFingerprintTemplate,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.targetService = targetService;
        this.metricKey = metricKey;
        this.threshold = threshold;
        this.windowMinutes = windowMinutes;
        this.comparison = comparison;
        this.dedupeFingerprintTemplate = dedupeFingerprintTemplate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public MetricKey getMetricKey() {
        return metricKey;
    }

    public void setMetricKey(MetricKey metricKey) {
        this.metricKey = metricKey;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public ComparisonOperator getComparison() {
        return comparison;
    }

    public void setComparison(ComparisonOperator comparison) {
        this.comparison = comparison;
    }

    public String getDedupeFingerprintTemplate() {
        return dedupeFingerprintTemplate;
    }

    public void setDedupeFingerprintTemplate(String dedupeFingerprintTemplate) {
        this.dedupeFingerprintTemplate = dedupeFingerprintTemplate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
