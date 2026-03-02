package com.yourorg.tourism.guide.dto;

import java.util.UUID;

import com.yourorg.tourism.guide.entity.VerificationLevel;
import com.yourorg.tourism.guide.entity.VerificationStatus;

public record GuideProfileResponseDto(
        UUID guideId,
        String firstName,
        String lastName,
        String email,
        VerificationLevel verificationLevel,
        VerificationStatus verificationStatus,
        boolean trustBadge
) {
}
