package com.yourorg.tourism.booking.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yourorg.tourism.common.audit.service.AuditEventService;
import com.yourorg.tourism.user.entity.UserRole;

@Service
public class BookingAuditService {

    private final AuditEventService auditEventService;

    public BookingAuditService(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    public void record(UUID actorUserId, String action, UUID bookingId, String metadata) {
        auditEventService.record(actorUserId, action, "BOOKING", bookingId, metadata);
    }

    public void recordStatusTransition(
            UUID actorUserId,
            UserRole actorRole,
            UUID bookingId,
            String action,
            String beforeStatus,
            String afterStatus
    ) {
        auditEventService.record(
                actorUserId,
                actorRole.name(),
                action,
                "BOOKING",
                bookingId,
                "{\"status\":\"" + beforeStatus + "\"}",
                "{\"status\":\"" + afterStatus + "\"}"
        );
    }
}
