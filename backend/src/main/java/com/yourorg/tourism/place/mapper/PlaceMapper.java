package com.yourorg.tourism.place.mapper;

import org.springframework.stereotype.Component;

import com.yourorg.tourism.place.dto.CreatePlaceRequestDto;
import com.yourorg.tourism.place.dto.PlaceResponseDto;
import com.yourorg.tourism.place.entity.PlaceEntity;

@Component
public class PlaceMapper {

    public PlaceEntity toEntity(CreatePlaceRequestDto request) {
        PlaceEntity entity = new PlaceEntity();
        entity.setTitle(request.title().trim());
        entity.setDescription(request.description());
        entity.setAddress(request.address());
        entity.setMapUrl(request.mapUrl());
        return entity;
    }

    public PlaceResponseDto toResponse(PlaceEntity entity) {
        return new PlaceResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getMapUrl(),
                entity.getCreatedAt()
        );
    }
}
