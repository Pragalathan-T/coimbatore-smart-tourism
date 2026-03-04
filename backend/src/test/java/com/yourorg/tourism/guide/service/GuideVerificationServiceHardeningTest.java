package com.yourorg.tourism.guide.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

import com.yourorg.tourism.common.audit.service.AuditEventService;
import com.yourorg.tourism.common.config.PaginationGuard;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.guide.dto.ApplyGuideVerificationRequestDto;
import com.yourorg.tourism.guide.entity.DocumentType;
import com.yourorg.tourism.guide.entity.GuideVerificationEntity;
import com.yourorg.tourism.guide.entity.VerificationLevel;
import com.yourorg.tourism.guide.entity.VerificationStatus;
import com.yourorg.tourism.guide.mapper.GuideVerificationMapper;
import com.yourorg.tourism.guide.repository.GuideVerificationAuditRepository;
import com.yourorg.tourism.guide.repository.GuideVerificationRepository;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

class GuideVerificationServiceHardeningTest {

    @Test
    void applyShouldReturn409InvalidStateWhenPendingExists() {
        GuideVerificationRepository verificationRepository = mock(GuideVerificationRepository.class);
        UserService userService = mock(UserService.class);
        GuideVerificationService service = new GuideVerificationService(
                verificationRepository,
                mock(GuideVerificationAuditRepository.class),
                mock(GuideVerificationMapper.class),
                userService,
                new PaginationGuard(100),
                mock(AuditEventService.class),
                7
        );

        UUID guideId = UUID.randomUUID();
        when(userService.getById(guideId)).thenReturn(new UserResponseDto(
                guideId, "Guide", "One", "guide@example.com", UserRole.GUIDE, true, Instant.now()));
        when(verificationRepository.existsByGuideIdAndStatus(guideId, VerificationStatus.PENDING)).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> service.apply(guideId, new ApplyGuideVerificationRequestDto(
                        DocumentType.AADHAR,
                        "A123456789",
                        "https://example.com/aadhar.pdf"
                )));

        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals(ErrorCode.INVALID_STATE, ex.getErrorCode());
    }

    @Test
    void applyShouldRespectRejectedCooldown() {
        GuideVerificationRepository verificationRepository = mock(GuideVerificationRepository.class);
        UserService userService = mock(UserService.class);
        GuideVerificationService service = new GuideVerificationService(
                verificationRepository,
                mock(GuideVerificationAuditRepository.class),
                mock(GuideVerificationMapper.class),
                userService,
                new PaginationGuard(100),
                mock(AuditEventService.class),
                7
        );

        UUID guideId = UUID.randomUUID();
        when(userService.getById(guideId)).thenReturn(new UserResponseDto(
                guideId, "Guide", "One", "guide@example.com", UserRole.GUIDE, true, Instant.now()));
        when(verificationRepository.existsByGuideIdAndStatus(guideId, VerificationStatus.PENDING)).thenReturn(false);

        GuideVerificationEntity rejected = new GuideVerificationEntity();
        rejected.setGuideId(guideId);
        rejected.setStatus(VerificationStatus.REJECTED);
        rejected.setUpdatedAt(Instant.now().minusSeconds(24 * 60 * 60));
        when(verificationRepository.findTopByGuideIdAndStatusOrderByCreatedAtDesc(guideId, VerificationStatus.REJECTED))
                .thenReturn(Optional.of(rejected));

        AppException ex = assertThrows(AppException.class,
                () -> service.apply(guideId, new ApplyGuideVerificationRequestDto(
                        DocumentType.AADHAR,
                        "A123456789",
                        "https://example.com/aadhar.pdf"
                )));

        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals(ErrorCode.INVALID_STATE, ex.getErrorCode());
    }

    @Test
    void approveShouldRejectWhenVerificationLevelIsNotBasic() {
        GuideVerificationRepository verificationRepository = mock(GuideVerificationRepository.class);
        UserService userService = mock(UserService.class);
        GuideVerificationService service = new GuideVerificationService(
                verificationRepository,
                mock(GuideVerificationAuditRepository.class),
                mock(GuideVerificationMapper.class),
                userService,
                new PaginationGuard(100),
                mock(AuditEventService.class),
                7
        );

        UUID adminId = UUID.randomUUID();
        when(userService.getById(adminId)).thenReturn(new UserResponseDto(
                adminId, "Admin", "One", "admin@example.com", UserRole.ADMIN, true, Instant.now()));

        GuideVerificationEntity verification = new GuideVerificationEntity();
        verification.setId(UUID.randomUUID());
        verification.setGuideId(UUID.randomUUID());
        verification.setStatus(VerificationStatus.PENDING);
        verification.setVerificationLevel(VerificationLevel.ID_VERIFIED);
        when(verificationRepository.findById(verification.getId())).thenReturn(Optional.of(verification));

        AppException ex = assertThrows(AppException.class,
                () -> service.approve(verification.getId(), adminId));

        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals(ErrorCode.INVALID_STATE, ex.getErrorCode());
        verify(userService, never()).setRoleAndIncrementTokenVersion(any(), any());
    }
}
