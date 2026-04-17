package com.servicelens.platform.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "evidence_refs")
public class EvidenceRef {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 32)
    private EvidenceType evidenceType;

    @Column(length = 512)
    private String label;

    @Column(name = "ref_text", nullable = false, columnDefinition = "text")
    private String refText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected EvidenceRef() {
    }

    public EvidenceRef(UUID id, Incident incident, EvidenceType evidenceType, String label, String refText,
            JsonNode metadata, Instant createdAt) {
        this.id = id;
        this.incident = incident;
        this.evidenceType = evidenceType;
        this.label = label;
        this.refText = refText;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public String getLabel() {
        return label;
    }

    public String getRefText() {
        return refText;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
