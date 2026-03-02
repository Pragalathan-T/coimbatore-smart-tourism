package com.yourorg.tourism.auth.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "AuthGuideVerificationEntity")
@Table(name = "guide_verification")
public class GuideVerificationEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "id_proof_url", nullable = false)
    private String idProofUrl;

    @Column(name = "selfie_url", nullable = false)
    private String selfieUrl;

    @Column(name = "verification_status", nullable = false, length = 20)
    private String verificationStatus;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getIdProofUrl() {
        return idProofUrl;
    }

    public void setIdProofUrl(String idProofUrl) {
        this.idProofUrl = idProofUrl;
    }

    public String getSelfieUrl() {
        return selfieUrl;
    }

    public void setSelfieUrl(String selfieUrl) {
        this.selfieUrl = selfieUrl;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
