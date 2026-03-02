package com.yourorg.tourism.place.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PlaceService(PlaceRepository placeRepository, PlaceMapper placeMapper) {
        this.placeRepository = placeRepository;
        this.placeMapper = placeMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<PlaceResponseDto> list(int page, int size) {
        Page<PlaceEntity> places = placeRepository.findAll(PageRequest.of(page, size));
        return PageResponse.from(places.map(placeMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PlaceResponseDto getById(UUID id) {
        PlaceEntity place = placeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Place not found"));
        return placeMapper.toResponse(place);
    }

    @Transactional
    public PlaceResponseDto create(CreatePlaceRequestDto request) {
        PlaceEntity saved = placeRepository.save(placeMapper.toEntity(request));
        return placeMapper.toResponse(saved);
    }

    @Transactional
    public PlaceResponseDto update(UUID id, UpdatePlaceRequestDto request) {
        PlaceEntity place = placeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Place not found"));

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
        return placeMapper.toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        PlaceEntity place = placeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Place not found"));
        placeRepository.delete(place);
    }
}
