package com.yourorg.tourism.guide.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectGuideVerificationRequestDto(
        @NotBlank String rejectionReason
) {
}
