package com.servicelens.platform.api.dto;

public record PatchRuleRequest(Boolean enabled, Double threshold, Integer windowMinutes) {
}
