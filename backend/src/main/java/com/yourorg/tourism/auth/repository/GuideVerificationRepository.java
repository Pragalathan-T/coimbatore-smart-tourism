package com.yourorg.tourism.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.auth.entity.GuideVerificationEntity;

public interface GuideVerificationRepository extends JpaRepository<GuideVerificationEntity, UUID> {
}
