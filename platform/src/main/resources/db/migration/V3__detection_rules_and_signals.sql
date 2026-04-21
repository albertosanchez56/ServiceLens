CREATE TABLE IF NOT EXISTS detection_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(512) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    target_service VARCHAR(256),
    metric_key VARCHAR(64) NOT NULL,
    threshold DOUBLE PRECISION NOT NULL,
    window_minutes INT NOT NULL DEFAULT 5,
    comparison VARCHAR(8) NOT NULL,
    dedupe_fingerprint_template VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS detection_signals (
    id UUID PRIMARY KEY,
    rule_id UUID NOT NULL REFERENCES detection_rules(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    fingerprint VARCHAR(256) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    incident_id UUID REFERENCES incidents(id) ON DELETE SET NULL,
    payload JSONB NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    window_end TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (rule_id, fingerprint)
);

CREATE INDEX IF NOT EXISTS idx_detection_signals_rule ON detection_signals(rule_id);
CREATE INDEX IF NOT EXISTS idx_detection_signals_incident ON detection_signals(incident_id);

-- Dos reglas demo: tasa de errores 5xx en payment y latencia p95 en checkout (umbrales orientativos).
INSERT INTO detection_rules (
    id, name, enabled, target_service, metric_key, threshold, window_minutes, comparison,
    dedupe_fingerprint_template, created_at, updated_at
) VALUES
    (
        'a0000000-0000-4000-8000-000000000001',
        'Payment — tasa errores HTTP 5xx',
        TRUE,
        'demo-payment',
        'ERROR_RATE_5XX',
        0,
        5,
        'GT',
        NULL,
        NOW(),
        NOW()
    ),
    (
        'a0000000-0000-4000-8000-000000000002',
        'Checkout — latencia p95 (s)',
        TRUE,
        'demo-checkout',
        'LATENCY_P95',
        5.0,
        5,
        'GT',
        NULL,
        NOW(),
        NOW()
    );
