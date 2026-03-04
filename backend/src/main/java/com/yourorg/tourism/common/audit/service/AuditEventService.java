package com.yourorg.tourism.common.audit.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.common.audit.entity.AuditEventEntity;
import com.yourorg.tourism.common.audit.repository.AuditEventRepository;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void record(UUID actorUserId, String action, String entityType, UUID entityId, String metadata) {
        record(actorUserId, null, action, entityType, entityId, null, metadata);
    }

    @Transactional
    public void record(
            UUID actorUserId,
            String actorRole,
            String action,
            String entityType,
            UUID entityId,
            String beforeJson,
            String afterJson
    ) {
        AuditEventEntity event = new AuditEventEntity();
        event.setActorUserId(actorUserId);
        event.setActorRole(actorRole);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setBeforeJson(beforeJson);
        event.setAfterJson(afterJson);
        event.setMetadata(afterJson);
        auditEventRepository.save(event);
    }
}
