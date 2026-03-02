package com.yourorg.tourism.guide.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.ApiResponse;
import com.yourorg.tourism.guide.dto.ApplyGuideVerificationRequestDto;
import com.yourorg.tourism.guide.dto.GuideVerificationResponseDto;
import com.yourorg.tourism.guide.service.GuideVerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/guides/verification")
@Validated
public class GuideVerificationController {

    private final GuideVerificationService guideVerificationService;

    public GuideVerificationController(GuideVerificationService guideVerificationService) {
        this.guideVerificationService = guideVerificationService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<GuideVerificationResponseDto>> apply(
            @Valid @RequestBody ApplyGuideVerificationRequestDto request
    ) {
        GuideVerificationResponseDto response = guideVerificationService.apply(currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ApiResponse<GuideVerificationResponseDto> getMyVerification() {
        return ApiResponse.success(guideVerificationService.getMy(currentUserId()));
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
