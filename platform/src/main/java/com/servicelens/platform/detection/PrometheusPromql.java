package com.servicelens.platform.detection;

import com.servicelens.platform.domain.DetectionRule;
import com.servicelens.platform.domain.MetricKey;

/**
 * Construye consultas instantáneas compatibles con la API query de Prometheus.
 * Métricas Micrometer/Spring Boot 3: {@code http_server_requests_seconds_*} con etiqueta {@code job}
 * según {@code infra/prometheus.yml}.
 */
public final class PrometheusPromql {

    private PrometheusPromql() {
    }

    public static String build(DetectionRule rule) {
        String job = rule.getTargetService();
        if (job == null || job.isBlank()) {
            throw new IllegalArgumentException("targetService (job Prometheus) requerido");
        }
        int w = rule.getWindowMinutes();
        return switch (rule.getMetricKey()) {
            case ERROR_RATE_5XX ->
                    // Rango corto para rate(): con scrape ~15s, [2m] reacciona antes que [5m].
                    // Spring Boot 3 puede etiquetar con status (500) y/o outcome (SERVER_ERROR); usamos or.
                    String.format(
                            "(sum(rate(http_server_requests_seconds_count{job=\"%s\",status=~\"5..\"}[2m])) "
                                    + "or sum(rate(http_server_requests_seconds_count{job=\"%s\",outcome=\"SERVER_ERROR\"}[2m])))",
                            escapeJob(job),
                            escapeJob(job));
            case LATENCY_P95 -> String.format(
                    "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job=\"%s\"}[%dm])) by (le))",
                    escapeJob(job),
                    w);
            case SIMULATED_PAYMENT_FAIL ->
                    // Valor instantáneo del contador (no rate/increase): con pocos scrapes, increase[5m] puede dar 0
                    // aunque el contador ya lleve >0 desde hace minutos.
                    String.format(
                            "sum(servicelens_demo_payment_simulated_failures_total{job=\"%s\"})",
                            escapeJob(job));
        };
    }

    public static String conditionSummary(DetectionRule rule) {
        try {
            String q = build(rule);
            if (q.length() > 200) {
                return rule.getMetricKey() + " " + rule.getComparison() + " " + rule.getThreshold() + " (ventana "
                        + rule.getWindowMinutes() + "m)";
            }
            return q;
        } catch (RuntimeException e) {
            return rule.getMetricKey() + " " + rule.getComparison() + " " + rule.getThreshold() + " (ventana "
                    + rule.getWindowMinutes() + "m)";
        }
    }

    private static String escapeJob(String job) {
        return job.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
