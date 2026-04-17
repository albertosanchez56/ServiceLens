package com.servicelens.platform.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.EvidenceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEvidenceRequest(
        @NotNull EvidenceType type,
        String label,
        @NotBlank String ref,
        JsonNode metadata
) {
}
