ALTER TABLE audit_events
    ADD COLUMN IF NOT EXISTS actor_role VARCHAR(20),
    ADD COLUMN IF NOT EXISTS before_json TEXT,
    ADD COLUMN IF NOT EXISTS after_json TEXT;

CREATE INDEX IF NOT EXISTS idx_audit_events_action_created_at ON audit_events(action, created_at);
