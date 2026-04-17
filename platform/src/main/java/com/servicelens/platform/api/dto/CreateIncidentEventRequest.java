package com.servicelens.platform.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.IncidentEventType;

import jakarta.validation.constraints.NotNull;

public record CreateIncidentEventRequest(
        @NotNull IncidentEventType type,
        @NotNull JsonNode payload
) {
}
