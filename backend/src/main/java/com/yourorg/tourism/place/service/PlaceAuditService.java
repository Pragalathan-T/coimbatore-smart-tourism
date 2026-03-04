package com.yourorg.tourism.place.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yourorg.tourism.common.audit.service.AuditEventService;
import com.yourorg.tourism.place.entity.PlaceEntity;
import com.yourorg.tourism.user.entity.UserRole;

@Service
public class PlaceAuditService {

    private final AuditEventService auditEventService;

    public PlaceAuditService(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    public void recordCreate(UUID actorUserId, PlaceEntity created) {
        auditEventService.record(
                actorUserId,
                UserRole.ADMIN.name(),
                "PLACE_CREATED",
                "PLACE",
                created.getId(),
                null,
                toPlaceJson(created)
        );
    }

    public void recordUpdate(UUID actorUserId, PlaceEntity before, PlaceEntity after) {
        auditEventService.record(
                actorUserId,
                UserRole.ADMIN.name(),
                "PLACE_UPDATED",
                "PLACE",
                after.getId(),
                toPlaceJson(before),
                toPlaceJson(after)
        );
    }

    public void recordDelete(UUID actorUserId, PlaceEntity before, PlaceEntity after) {
        auditEventService.record(
                actorUserId,
                UserRole.ADMIN.name(),
                "PLACE_DELETED",
                "PLACE",
                after.getId(),
                toPlaceJson(before),
                toPlaceJson(after)
        );
    }

    private String toPlaceJson(PlaceEntity place) {
        return "{" +
                "\"id\":\"" + place.getId() + "\"," +
                "\"title\":\"" + escape(place.getTitle()) + "\"," +
                "\"description\":" + asJsonStringOrNull(place.getDescription()) + "," +
                "\"address\":" + asJsonStringOrNull(place.getAddress()) + "," +
                "\"mapUrl\":" + asJsonStringOrNull(place.getMapUrl()) + "," +
                "\"deletedAt\":" + asJsonStringOrNull(place.getDeletedAt() == null ? null : place.getDeletedAt().toString()) +
                "}";
    }

    private String asJsonStringOrNull(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
