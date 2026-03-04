package com.yourorg.tourism.common.audit.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.common.audit.entity.AuditEventEntity;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
}
