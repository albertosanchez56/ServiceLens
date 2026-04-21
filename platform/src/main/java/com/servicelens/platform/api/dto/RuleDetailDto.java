package com.servicelens.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

public record RuleDetailDto(
        UUID id,
        String name,
        boolean enabled,
        String targetService,
        String metricKey,
        double threshold,
        int windowMinutes,
        String comparison,
        String dedupeFingerprintTemplate,
        Instant createdAt,
        Instant updatedAt) {
}
