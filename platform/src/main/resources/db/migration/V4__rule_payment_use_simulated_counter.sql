-- Regla demo: usar contador explícito en payment (fiable frente a etiquetas HTTP en Spring Boot 3).
UPDATE detection_rules
SET metric_key = 'SIMULATED_PAYMENT_FAIL',
    updated_at   = NOW()
WHERE id = 'a0000000-0000-4000-8000-000000000001';
