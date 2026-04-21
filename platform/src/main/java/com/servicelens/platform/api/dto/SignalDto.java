package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record SignalDto(
        UUID id,
        UUID ruleId,
        String status,
        String fingerprint,
        Instant occurredAt,
        UUID incidentId,
        JsonNode payload) {
}
