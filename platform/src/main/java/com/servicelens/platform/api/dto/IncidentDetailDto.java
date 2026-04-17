package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.IncidentSeverity;
import com.servicelens.platform.domain.IncidentState;

public record IncidentDetailDto(
        UUID id,
        String title,
        IncidentState state,
        IncidentSeverity severity,
        Instant startedAt,
        Instant updatedAt,
        Instant resolvedAt,
        String rootService,
        String summary,
        JsonNode metadata,
        long version
) {
}
