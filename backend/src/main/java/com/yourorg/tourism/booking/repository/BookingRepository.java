package com.yourorg.tourism.booking.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.booking.entity.BookingEntity;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

	Page<BookingEntity> findAllByTouristId(UUID touristId, Pageable pageable);

	Page<BookingEntity> findAllByGuideId(UUID guideId, Pageable pageable);

	Page<BookingEntity> findAllByTouristIdOrGuideId(UUID touristId, UUID guideId, Pageable pageable);
}
