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
@Table(name = "incident_events")
public class IncidentEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private IncidentEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_username", length = 256)
    private String actorUsername;

    @Column(name = "system_actor", nullable = false)
    private boolean systemActor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    protected IncidentEvent() {
    }

    public IncidentEvent(UUID id, Incident incident, IncidentEventType eventType, Instant occurredAt,
            String actorUsername, boolean systemActor, JsonNode payload) {
        this.id = id;
        this.incident = incident;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.actorUsername = actorUsername;
        this.systemActor = systemActor;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public IncidentEventType getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public boolean isSystemActor() {
        return systemActor;
    }

    public JsonNode getPayload() {
        return payload;
    }
}
