package com.yourorg.tourism.guide.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.ApiResponse;
import com.yourorg.tourism.common.response.PageResponse;
import com.yourorg.tourism.guide.dto.GuideVerificationResponseDto;
import com.yourorg.tourism.guide.dto.RejectGuideVerificationRequestDto;
import com.yourorg.tourism.guide.entity.VerificationStatus;
import com.yourorg.tourism.guide.service.GuideVerificationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/admin/verifications")
@Validated
public class AdminVerificationController {

    private final GuideVerificationService guideVerificationService;

    public AdminVerificationController(GuideVerificationService guideVerificationService) {
        this.guideVerificationService = guideVerificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<GuideVerificationResponseDto>> listByStatus(
            @RequestParam(defaultValue = "PENDING") VerificationStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return ApiResponse.success(guideVerificationService.listByStatus(status, page, size));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<GuideVerificationResponseDto> approve(@PathVariable UUID id) {
        return ApiResponse.success(guideVerificationService.approve(id, currentUserId()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<GuideVerificationResponseDto>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectGuideVerificationRequestDto request
    ) {
        GuideVerificationResponseDto response = guideVerificationService.reject(id, request.rejectionReason(), currentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
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
