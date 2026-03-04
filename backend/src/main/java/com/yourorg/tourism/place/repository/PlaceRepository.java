package com.yourorg.tourism.place.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.place.entity.PlaceEntity;

public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {

	Page<PlaceEntity> findAllByDeletedAtIsNull(Pageable pageable);

	Optional<PlaceEntity> findByIdAndDeletedAtIsNull(UUID id);
}
