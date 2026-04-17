-- Esqueleto Sprint 1: tabla placeholder hasta dominio incidente (Sprint 2)
CREATE TABLE IF NOT EXISTS servicelens_meta (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO servicelens_meta DEFAULT VALUES;
