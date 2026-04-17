package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicelens.platform.domain.EvidenceType;

public record EvidenceResponse(
        UUID id,
        EvidenceType type,
        String label,
        String ref,
        JsonNode metadata,
        Instant createdAt
) {
}
