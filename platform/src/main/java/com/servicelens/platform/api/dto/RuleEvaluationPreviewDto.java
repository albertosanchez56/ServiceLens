package com.servicelens.platform.api.dto;

public record RuleEvaluationPreviewDto(
        String prometheusBaseUrl,
        String promql,
        /** Vacío si Prometheus no respondió o el parseo falló (no confundir con valor numérico 0). */
        boolean prometheusResultEmpty,
        double observed,
        boolean breach,
        String threshold,
        String comparison) {
}
