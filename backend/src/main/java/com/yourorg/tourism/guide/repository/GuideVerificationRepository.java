package com.yourorg.tourism.guide.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.guide.entity.GuideVerificationEntity;
import com.yourorg.tourism.guide.entity.VerificationStatus;

public interface GuideVerificationRepository extends JpaRepository<GuideVerificationEntity, UUID> {

    boolean existsByGuideIdAndStatus(UUID guideId, VerificationStatus status);

    Optional<GuideVerificationEntity> findTopByGuideIdOrderByCreatedAtDesc(UUID guideId);

    Optional<GuideVerificationEntity> findTopByGuideIdAndStatusOrderByUpdatedAtDesc(UUID guideId, VerificationStatus status);

    Page<GuideVerificationEntity> findAllByStatus(VerificationStatus status, Pageable pageable);
}
