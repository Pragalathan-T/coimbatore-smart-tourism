package com.yourorg.tourism.guide.service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.common.audit.service.AuditEventService;
import com.yourorg.tourism.common.config.PaginationGuard;
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
    private final PaginationGuard paginationGuard;
    private final AuditEventService auditEventService;
    private final int cooldownDays;

    public GuideVerificationService(
            GuideVerificationRepository verificationRepository,
            GuideVerificationAuditRepository auditRepository,
            GuideVerificationMapper verificationMapper,
            UserService userService,
            PaginationGuard paginationGuard,
            AuditEventService auditEventService,
            @Value("${app.guide.verification.cooldown-days:7}") int cooldownDays
    ) {
        this.verificationRepository = verificationRepository;
        this.auditRepository = auditRepository;
        this.verificationMapper = verificationMapper;
        this.userService = userService;
        this.paginationGuard = paginationGuard;
        this.auditEventService = auditEventService;
        this.cooldownDays = cooldownDays;
    }

    @Transactional
    public GuideVerificationResponseDto apply(UUID authenticatedUserId, ApplyGuideVerificationRequestDto request) {
        ensureRole(authenticatedUserId, UserRole.GUIDE);

        if (verificationRepository.existsByGuideIdAndStatus(authenticatedUserId, VerificationStatus.PENDING)) {
            throw new AppException(ErrorCode.INVALID_STATE, HttpStatus.CONFLICT, "Pending verification already exists");
        }

        verificationRepository.findTopByGuideIdAndStatusOrderByCreatedAtDesc(authenticatedUserId, VerificationStatus.REJECTED)
                .ifPresent(lastRejected -> {
                    if (lastRejected.getUpdatedAt() != null
                            && lastRejected.getUpdatedAt().plusSeconds(cooldownDays * 24L * 60L * 60L).isAfter(java.time.Instant.now())) {
                        String reapplyDate = lastRejected.getUpdatedAt()
                                .plusSeconds(cooldownDays * 24L * 60L * 60L)
                                .atOffset(ZoneOffset.UTC)
                                .toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE);
                        throw new AppException(
                                ErrorCode.INVALID_STATE,
                                HttpStatus.CONFLICT,
                                "Reapply after " + reapplyDate
                        );
                    }
                });

        GuideVerificationEntity entity = verificationMapper.toEntity(authenticatedUserId, request);
        try {
            GuideVerificationEntity saved = verificationRepository.save(entity);
            return verificationMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.INVALID_STATE, HttpStatus.CONFLICT, "Pending verification already exists");
        }
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
        paginationGuard.validateSize(size);
        Page<GuideVerificationEntity> results = verificationRepository.findAllByStatus(status, PageRequest.of(page, size));
        return PageResponse.from(results.map(verificationMapper::toResponse));
    }

    @Transactional
    public GuideVerificationResponseDto approve(UUID verificationId, UUID adminUserId) {
        ensureRole(adminUserId, UserRole.ADMIN);

        GuideVerificationEntity verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Verification not found"));

        if (verification.getStatus() != VerificationStatus.PENDING
            || verification.getVerificationLevel() != VerificationLevel.BASIC) {
            throw new AppException(
                    ErrorCode.INVALID_STATE,
                    HttpStatus.CONFLICT,
                "Invalid verification state transition: only BASIC PENDING verifications can be approved"
            );
        }

        String beforeJson = verificationSnapshotJson(verification);

        verification.setStatus(VerificationStatus.APPROVED);
        verification.setRejectionReason(null);
        if (!verification.getVerificationLevel().isAtLeast(VerificationLevel.ID_VERIFIED)) {
            verification.setVerificationLevel(VerificationLevel.ID_VERIFIED);
        }

        GuideVerificationEntity saved = verificationRepository.save(verification);
        userService.setRoleAndIncrementTokenVersion(saved.getGuideId(), UserRole.GUIDE);
        auditRepository.save(verificationMapper.toAudit(saved.getId(), VerificationAuditAction.APPROVED, adminUserId, null));
        auditEventService.record(
            adminUserId,
            UserRole.ADMIN.name(),
            "VERIFICATION_APPROVED",
            "GUIDE_VERIFICATION",
            saved.getId(),
            beforeJson,
            verificationSnapshotJson(saved)
        );
        auditEventService.record(
            adminUserId,
            UserRole.ADMIN.name(),
            "ROLE_PROMOTED_GUIDE",
            "USER",
            saved.getGuideId(),
            "{\"role\":\"TOURIST_OR_GUIDE\"}",
            "{\"role\":\"GUIDE\",\"tokenVersionIncremented\":true}"
        );
        return verificationMapper.toResponse(saved);
    }

    @Transactional
    public GuideVerificationResponseDto reject(UUID verificationId, String rejectionReason, UUID adminUserId) {
        ensureRole(adminUserId, UserRole.ADMIN);

        GuideVerificationEntity verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Verification not found"));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new AppException(
                    ErrorCode.INVALID_STATE,
                    HttpStatus.CONFLICT,
                    "Invalid verification state transition: only PENDING verifications can be rejected"
            );
        }

        String beforeJson = verificationSnapshotJson(verification);
        verification.setStatus(VerificationStatus.REJECTED);
        verification.setRejectionReason(rejectionReason.trim());

        GuideVerificationEntity saved = verificationRepository.save(verification);
        auditRepository.save(verificationMapper.toAudit(
                saved.getId(),
                VerificationAuditAction.REJECTED,
                adminUserId,
                rejectionReason.trim()
        ));
            auditEventService.record(
                adminUserId,
                UserRole.ADMIN.name(),
                "VERIFICATION_REJECTED",
                "GUIDE_VERIFICATION",
                saved.getId(),
                beforeJson,
                verificationSnapshotJson(saved)
            );
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

    private String verificationSnapshotJson(GuideVerificationEntity verification) {
        return "{" +
                "\"status\":\"" + verification.getStatus().name() + "\"," +
                "\"verificationLevel\":\"" + verification.getVerificationLevel().name() + "\"," +
                "\"rejectionReason\":" + (verification.getRejectionReason() == null
                ? "null"
                : "\"" + verification.getRejectionReason().replace("\"", "\\\"") + "\"") +
                "}";
    }
}
