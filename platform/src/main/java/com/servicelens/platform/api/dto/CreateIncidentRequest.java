package com.servicelens.platform.api.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.IncidentSeverity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 512) String title,
        @NotNull IncidentSeverity severity,
        Instant startedAt,
        String rootService,
        JsonNode metadata
) {
}
