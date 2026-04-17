CREATE TABLE IF NOT EXISTS incidents (
    id UUID PRIMARY KEY,
    title VARCHAR(512) NOT NULL,
    state VARCHAR(32) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    root_service VARCHAR(256),
    summary TEXT,
    metadata JSONB,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS incident_events (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    actor_username VARCHAR(256),
    system_actor BOOLEAN NOT NULL DEFAULT false,
    payload JSONB NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_incident_events_incident_occurred ON incident_events(incident_id, occurred_at);

CREATE TABLE IF NOT EXISTS evidence_refs (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    evidence_type VARCHAR(32) NOT NULL,
    label VARCHAR(512),
    ref_text TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_evidence_incident ON evidence_refs(incident_id);
