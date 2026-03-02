package com.yourorg.tourism.guide.dto;

import java.time.Instant;
import java.util.UUID;

import com.yourorg.tourism.guide.entity.DocumentType;
import com.yourorg.tourism.guide.entity.VerificationLevel;
import com.yourorg.tourism.guide.entity.VerificationStatus;

public record GuideVerificationResponseDto(
        UUID id,
        UUID guideId,
        VerificationLevel verificationLevel,
        VerificationStatus status,
        DocumentType documentType,
        String documentNumber,
        String documentUrl,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {
}
