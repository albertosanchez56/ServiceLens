package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.servicelens.platform.domain.IncidentSeverity;
import com.servicelens.platform.domain.IncidentState;

public record IncidentSummaryDto(
        UUID id,
        String title,
        IncidentState state,
        IncidentSeverity severity,
        Instant startedAt,
        Instant updatedAt,
        String rootService,
        long signalCount,
        long eventCount
) {
}
