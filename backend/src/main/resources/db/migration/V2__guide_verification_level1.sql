CREATE TABLE guide_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guide_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verification_level VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    document_type VARCHAR(30) NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    document_url TEXT NOT NULL,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_guide_verifications_level CHECK (verification_level IN ('BASIC', 'ID_VERIFIED', 'ADDRESS_VERIFIED', 'FULLY_VERIFIED')),
    CONSTRAINT chk_guide_verifications_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_guide_verifications_document_type CHECK (document_type IN ('AADHAR', 'PASSPORT', 'DRIVING_LICENSE'))
);

CREATE UNIQUE INDEX uq_guide_verifications_pending_guide
    ON guide_verifications (guide_id)
    WHERE status = 'PENDING';

CREATE TABLE guide_verification_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    verification_id UUID NOT NULL REFERENCES guide_verifications(id) ON DELETE CASCADE,
    action VARCHAR(20) NOT NULL,
    performed_by UUID NOT NULL REFERENCES users(id),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_guide_verification_audit_action CHECK (action IN ('APPROVED', 'REJECTED'))
);
