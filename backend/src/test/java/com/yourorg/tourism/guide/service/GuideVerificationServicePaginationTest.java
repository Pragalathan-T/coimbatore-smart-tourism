package com.yourorg.tourism.guide.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.yourorg.tourism.common.audit.service.AuditEventService;
import com.yourorg.tourism.common.config.PaginationGuard;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.guide.entity.VerificationStatus;
import com.yourorg.tourism.guide.mapper.GuideVerificationMapper;
import com.yourorg.tourism.guide.repository.GuideVerificationAuditRepository;
import com.yourorg.tourism.guide.repository.GuideVerificationRepository;
import com.yourorg.tourism.user.service.UserService;

class GuideVerificationServicePaginationTest {

    @Test
    void shouldRejectTooLargePageSize() {
        GuideVerificationService service = new GuideVerificationService(
                mock(GuideVerificationRepository.class),
                mock(GuideVerificationAuditRepository.class),
                mock(GuideVerificationMapper.class),
                mock(UserService.class),
                new PaginationGuard(100),
                mock(AuditEventService.class),
                7
        );

        AppException ignored = assertThrows(AppException.class,
            () -> service.listByStatus(VerificationStatus.PENDING, 0, 101));
    }
}
