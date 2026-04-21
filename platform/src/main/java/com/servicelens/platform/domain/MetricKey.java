package com.servicelens.platform.domain;

public enum MetricKey {
    /** Tasa 5xx desde métricas HTTP de Spring (Prometheus). */
    ERROR_RATE_5XX,
    LATENCY_P95,
    /**
     * Contador demo en payment ({@code servicelens_demo_payment_simulated_failures_total});
     * fiable para la prueba de detección sin depender de etiquetas HTTP.
     */
    SIMULATED_PAYMENT_FAIL
}
