package com.servicelens.platform.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.IncidentSeverity;
import com.servicelens.platform.domain.IncidentState;

import jakarta.validation.constraints.Size;

public record PatchIncidentRequest(
        @Size(max = 512) String title,
        IncidentSeverity severity,
        IncidentState state,
        String rootService,
        JsonNode metadata,
        Long expectedVersion
) {
}
