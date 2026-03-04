CREATE UNIQUE INDEX IF NOT EXISTS uq_guide_verifications_pending_guide
    ON guide_verifications (guide_id)
    WHERE status = 'PENDING';
