package com.yourorg.tourism.guide.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.guide.entity.GuideVerificationAuditEntity;

public interface GuideVerificationAuditRepository extends JpaRepository<GuideVerificationAuditEntity, UUID> {
}
