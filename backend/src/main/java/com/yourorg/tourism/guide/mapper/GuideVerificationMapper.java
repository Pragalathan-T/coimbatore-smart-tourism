package com.yourorg.tourism.guide.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.yourorg.tourism.guide.dto.ApplyGuideVerificationRequestDto;
import com.yourorg.tourism.guide.dto.GuideProfileResponseDto;
import com.yourorg.tourism.guide.dto.GuideVerificationResponseDto;
import com.yourorg.tourism.guide.entity.GuideVerificationAuditEntity;
import com.yourorg.tourism.guide.entity.GuideVerificationEntity;
import com.yourorg.tourism.guide.entity.VerificationAuditAction;
import com.yourorg.tourism.guide.entity.VerificationLevel;
import com.yourorg.tourism.guide.entity.VerificationStatus;
import com.yourorg.tourism.user.dto.UserResponseDto;

@Component
public class GuideVerificationMapper {

    public GuideVerificationEntity toEntity(UUID guideId, ApplyGuideVerificationRequestDto request) {
        GuideVerificationEntity entity = new GuideVerificationEntity();
        entity.setGuideId(guideId);
        entity.setVerificationLevel(VerificationLevel.BASIC);
        entity.setStatus(VerificationStatus.PENDING);
        entity.setDocumentType(request.documentType());
        entity.setDocumentNumber(request.documentNumber().trim());
        entity.setDocumentUrl(request.documentUrl().trim());
        return entity;
    }

    public GuideVerificationResponseDto toResponse(GuideVerificationEntity entity) {
        return new GuideVerificationResponseDto(
                entity.getId(),
                entity.getGuideId(),
                entity.getVerificationLevel(),
                entity.getStatus(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getDocumentUrl(),
                entity.getRejectionReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public GuideVerificationAuditEntity toAudit(
            UUID verificationId,
            VerificationAuditAction action,
            UUID performedBy,
            String reason
    ) {
        GuideVerificationAuditEntity auditEntity = new GuideVerificationAuditEntity();
        auditEntity.setVerificationId(verificationId);
        auditEntity.setAction(action);
        auditEntity.setPerformedBy(performedBy);
        auditEntity.setReason(reason);
        return auditEntity;
    }

    public GuideProfileResponseDto toGuideProfile(
            UserResponseDto user,
            VerificationLevel verificationLevel,
            VerificationStatus verificationStatus
    ) {
        boolean trustBadge = verificationLevel == VerificationLevel.FULLY_VERIFIED
                && verificationStatus == VerificationStatus.APPROVED;

        return new GuideProfileResponseDto(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.email(),
                verificationLevel,
                verificationStatus,
                trustBadge
        );
    }
}
