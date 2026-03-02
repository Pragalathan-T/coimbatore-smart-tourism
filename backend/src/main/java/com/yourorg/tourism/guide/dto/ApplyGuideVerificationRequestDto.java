package com.yourorg.tourism.guide.dto;

import com.yourorg.tourism.guide.entity.DocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplyGuideVerificationRequestDto(
        @NotNull DocumentType documentType,
        @NotBlank @Size(max = 100) String documentNumber,
        @NotBlank String documentUrl
) {
}
