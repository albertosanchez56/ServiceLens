package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

public record RuleSummaryDto(
        UUID id,
        String name,
        boolean enabled,
        String targetService,
        String conditionSummary,
        Instant createdAt,
        Instant updatedAt) {
}
