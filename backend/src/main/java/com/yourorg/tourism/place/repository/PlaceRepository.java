package com.yourorg.tourism.place.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yourorg.tourism.place.entity.PlaceEntity;

public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {
}
