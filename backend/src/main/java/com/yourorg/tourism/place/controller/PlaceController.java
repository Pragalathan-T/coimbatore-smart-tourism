package com.yourorg.tourism.place.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.ApiResponse;
import com.yourorg.tourism.common.response.PageResponse;
import com.yourorg.tourism.place.dto.CreatePlaceRequestDto;
import com.yourorg.tourism.place.dto.PlaceResponseDto;
import com.yourorg.tourism.place.dto.UpdatePlaceRequestDto;
import com.yourorg.tourism.place.service.PlaceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@Validated
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/api/v1/places")
    public ApiResponse<PageResponse<PlaceResponseDto>> listPlaces(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return ApiResponse.success(placeService.list(page, size));
    }

    @GetMapping("/api/v1/places/{id}")
    public ApiResponse<PlaceResponseDto> getPlaceById(@PathVariable UUID id) {
        return ApiResponse.success(placeService.getById(id));
    }

    @PostMapping("/api/v1/admin/places")
    public ResponseEntity<ApiResponse<PlaceResponseDto>> createPlace(@Valid @RequestBody CreatePlaceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(placeService.create(request, currentUserId())));
    }

    @PutMapping("/api/v1/admin/places/{id}")
    public ApiResponse<PlaceResponseDto> updatePlace(@PathVariable UUID id, @Valid @RequestBody UpdatePlaceRequestDto request) {
        return ApiResponse.success(placeService.update(id, request, currentUserId()));
    }

    @DeleteMapping("/api/v1/admin/places/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(@PathVariable UUID id) {
        placeService.delete(id, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Place deleted"));
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }
}
