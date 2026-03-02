package com.yourorg.tourism.guide.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "guide_verification_audit")
public class GuideVerificationAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "verification_id", nullable = false, updatable = false)
    private UUID verificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20, updatable = false)
    private VerificationAuditAction action;

    @Column(name = "performed_by", nullable = false, updatable = false)
    private UUID performedBy;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(UUID verificationId) {
        this.verificationId = verificationId;
    }

    public VerificationAuditAction getAction() {
        return action;
    }

    public void setAction(VerificationAuditAction action) {
        this.action = action;
    }

    public UUID getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(UUID performedBy) {
        this.performedBy = performedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
