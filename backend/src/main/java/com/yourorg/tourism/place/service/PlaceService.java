package com.yourorg.tourism.place.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.common.config.PaginationGuard;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.PageResponse;
import com.yourorg.tourism.place.dto.CreatePlaceRequestDto;
import com.yourorg.tourism.place.dto.PlaceResponseDto;
import com.yourorg.tourism.place.dto.UpdatePlaceRequestDto;
import com.yourorg.tourism.place.entity.PlaceEntity;
import com.yourorg.tourism.place.mapper.PlaceMapper;
import com.yourorg.tourism.place.repository.PlaceRepository;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final PaginationGuard paginationGuard;
    private final PlaceAuditService placeAuditService;

    public PlaceService(
            PlaceRepository placeRepository,
            PlaceMapper placeMapper,
            PaginationGuard paginationGuard,
            PlaceAuditService placeAuditService
    ) {
        this.placeRepository = placeRepository;
        this.placeMapper = placeMapper;
        this.paginationGuard = paginationGuard;
        this.placeAuditService = placeAuditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PlaceResponseDto> list(int page, int size) {
        paginationGuard.validateSize(size);
        Page<PlaceEntity> places = placeRepository.findAllByDeletedAtIsNull(PageRequest.of(page, size));
        return PageResponse.from(places.map(placeMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PlaceResponseDto getById(UUID id) {
        PlaceEntity place = placeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Place not found"));
        return placeMapper.toResponse(place);
    }

    @Transactional
    public PlaceResponseDto create(CreatePlaceRequestDto request, UUID actorUserId) {
        PlaceEntity saved = placeRepository.save(placeMapper.toEntity(request));
        placeAuditService.recordCreate(actorUserId, saved);
        return placeMapper.toResponse(saved);
    }

    @Transactional
    public PlaceResponseDto update(UUID id, UpdatePlaceRequestDto request, UUID actorUserId) {
        PlaceEntity place = placeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Place not found"));
        PlaceEntity before = copyForAudit(place);

        if (request.title() != null) {
            place.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            place.setDescription(request.description());
        }
        if (request.address() != null) {
            place.setAddress(request.address());
        }
        if (request.mapUrl() != null) {
            place.setMapUrl(request.mapUrl());
        }

        PlaceEntity updated = placeRepository.save(place);
        placeAuditService.recordUpdate(actorUserId, before, updated);
        return placeMapper.toResponse(updated);
    }

    @Transactional
    public void delete(UUID id, UUID actorUserId) {
        PlaceEntity place = placeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Place not found"));
        PlaceEntity before = copyForAudit(place);
        place.setDeletedAt(Instant.now());
        place.setDeletedBy(actorUserId);
        PlaceEntity deleted = placeRepository.save(place);
        placeAuditService.recordDelete(actorUserId, before, deleted);
    }

    private PlaceEntity copyForAudit(PlaceEntity source) {
        PlaceEntity copy = new PlaceEntity();
        copy.setId(source.getId());
        copy.setTitle(source.getTitle());
        copy.setDescription(source.getDescription());
        copy.setAddress(source.getAddress());
        copy.setMapUrl(source.getMapUrl());
        copy.setDeletedAt(source.getDeletedAt());
        copy.setDeletedBy(source.getDeletedBy());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }
}
