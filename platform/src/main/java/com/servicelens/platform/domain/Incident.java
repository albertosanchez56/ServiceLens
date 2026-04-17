package com.servicelens.platform.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    private UUID id;

    @Column(nullable = false, length = 512)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentSeverity severity;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "root_service", length = 256)
    private String rootService;

    @Column(columnDefinition = "text")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @Version
    @Column(nullable = false)
    private long version;

    protected Incident() {
    }

    public Incident(UUID id, String title, IncidentSeverity severity, Instant startedAt, String rootService,
            JsonNode metadata) {
        this.id = id;
        this.title = title;
        this.state = IncidentState.OPEN;
        this.severity = severity;
        this.startedAt = startedAt;
        this.updatedAt = startedAt;
        this.rootService = rootService;
        this.metadata = metadata;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public IncidentState getState() {
        return state;
    }

    public void setState(IncidentState state) {
        this.state = state;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getRootService() {
        return rootService;
    }

    public void setRootService(String rootService) {
        this.rootService = rootService;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    public long getVersion() {
        return version;
    }
}
