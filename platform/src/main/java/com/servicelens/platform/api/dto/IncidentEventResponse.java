package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.IncidentEventType;

public record IncidentEventResponse(
        UUID id,
        UUID incidentId,
        IncidentEventType type,
        Instant occurredAt,
        ActorDto actor,
        JsonNode payload
) {
    public record ActorDto(String userId, boolean system) {
    }
}
