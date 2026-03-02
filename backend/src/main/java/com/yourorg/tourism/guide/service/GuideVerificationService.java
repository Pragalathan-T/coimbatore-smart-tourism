package com.yourorg.tourism.guide.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.PageResponse;
import com.yourorg.tourism.guide.dto.ApplyGuideVerificationRequestDto;
import com.yourorg.tourism.guide.dto.GuideProfileResponseDto;
import com.yourorg.tourism.guide.dto.GuideVerificationResponseDto;
import com.yourorg.tourism.guide.entity.GuideVerificationEntity;
import com.yourorg.tourism.guide.entity.VerificationAuditAction;
import com.yourorg.tourism.guide.entity.VerificationLevel;
import com.yourorg.tourism.guide.entity.VerificationStatus;
import com.yourorg.tourism.guide.mapper.GuideVerificationMapper;
import com.yourorg.tourism.guide.repository.GuideVerificationAuditRepository;
import com.yourorg.tourism.guide.repository.GuideVerificationRepository;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

@Service
public class GuideVerificationService {

    private final GuideVerificationRepository verificationRepository;
    private final GuideVerificationAuditRepository auditRepository;
    private final GuideVerificationMapper verificationMapper;
    private final UserService userService;

    public GuideVerificationService(
            GuideVerificationRepository verificationRepository,
            GuideVerificationAuditRepository auditRepository,
            GuideVerificationMapper verificationMapper,
            UserService userService
    ) {
        this.verificationRepository = verificationRepository;
        this.auditRepository = auditRepository;
        this.verificationMapper = verificationMapper;
        this.userService = userService;
    }

    @Transactional
    public GuideVerificationResponseDto apply(UUID authenticatedUserId, ApplyGuideVerificationRequestDto request) {
        ensureRole(authenticatedUserId, UserRole.GUIDE);

        if (verificationRepository.existsByGuideIdAndStatus(authenticatedUserId, VerificationStatus.PENDING)) {
            throw new AppException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Pending verification already exists");
        }

        GuideVerificationEntity entity = verificationMapper.toEntity(authenticatedUserId, request);
        GuideVerificationEntity saved = verificationRepository.save(entity);
        return verificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GuideVerificationResponseDto getMy(UUID authenticatedUserId) {
        ensureRole(authenticatedUserId, UserRole.GUIDE);

        GuideVerificationEntity verification = verificationRepository.findTopByGuideIdOrderByCreatedAtDesc(authenticatedUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Verification not found"));
        return verificationMapper.toResponse(verification);
    }

    @Transactional(readOnly = true)
    public PageResponse<GuideVerificationResponseDto> listByStatus(VerificationStatus status, int page, int size) {
        Page<GuideVerificationEntity> results = verificationRepository.findAllByStatus(status, PageRequest.of(page, size));
        return PageResponse.from(results.map(verificationMapper::toResponse));
    }

    @Transactional
    public GuideVerificationResponseDto approve(UUID verificationId, UUID adminUserId) {
        ensureRole(adminUserId, UserRole.ADMIN);

        GuideVerificationEntity verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Verification not found"));

        verification.setStatus(VerificationStatus.APPROVED);
        verification.setRejectionReason(null);
        if (!verification.getVerificationLevel().isAtLeast(VerificationLevel.ID_VERIFIED)) {
            verification.setVerificationLevel(VerificationLevel.ID_VERIFIED);
        }

        GuideVerificationEntity saved = verificationRepository.save(verification);
        auditRepository.save(verificationMapper.toAudit(saved.getId(), VerificationAuditAction.APPROVED, adminUserId, null));
        return verificationMapper.toResponse(saved);
    }

    @Transactional
    public GuideVerificationResponseDto reject(UUID verificationId, String rejectionReason, UUID adminUserId) {
        ensureRole(adminUserId, UserRole.ADMIN);

        GuideVerificationEntity verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Verification not found"));

        verification.setStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(rejectionReason.trim());

        GuideVerificationEntity saved = verificationRepository.save(verification);
        auditRepository.save(verificationMapper.toAudit(
                saved.getId(),
                VerificationAuditAction.REJECTED,
                adminUserId,
                rejectionReason.trim()
        ));
        return verificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public void assertGuideEligibleForBookingAcceptance(UUID guideId) {
        ensureRole(guideId, UserRole.GUIDE);

        GuideVerificationEntity approvedVerification = verificationRepository
                .findTopByGuideIdAndStatusOrderByUpdatedAtDesc(guideId, VerificationStatus.APPROVED)
                .orElseThrow(() -> new AppException(
                        ErrorCode.FORBIDDEN,
                        HttpStatus.FORBIDDEN,
                        "Guide verification is required to accept booking"
                ));

        if (!approvedVerification.getVerificationLevel().isAtLeast(VerificationLevel.ID_VERIFIED)) {
            throw new AppException(
                    ErrorCode.FORBIDDEN,
                    HttpStatus.FORBIDDEN,
                    "Guide verification level is insufficient to accept booking"
            );
        }
    }

    @Transactional(readOnly = true)
    public GuideProfileResponseDto getGuideProfile(UUID guideId) {
        UserResponseDto user = ensureRole(guideId, UserRole.GUIDE);

        GuideVerificationEntity verification = verificationRepository.findTopByGuideIdOrderByCreatedAtDesc(guideId)
                .orElse(null);

        VerificationLevel level = verification == null ? VerificationLevel.BASIC : verification.getVerificationLevel();
        VerificationStatus status = verification == null ? VerificationStatus.PENDING : verification.getStatus();
        return verificationMapper.toGuideProfile(user, level, status);
    }

    private UserResponseDto ensureRole(UUID userId, UserRole expectedRole) {
        UserResponseDto user = userService.getById(userId);
        if (user.role() != expectedRole) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Access denied");
        }
        return user;
    }
}
