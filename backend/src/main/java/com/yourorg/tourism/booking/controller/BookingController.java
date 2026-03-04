package com.yourorg.tourism.booking.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yourorg.tourism.booking.dto.BookingResponseDto;
import com.yourorg.tourism.booking.dto.CreateBookingRequestDto;
import com.yourorg.tourism.booking.service.BookingService;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.ApiResponse;
import com.yourorg.tourism.common.response.PageResponse;
import com.yourorg.tourism.user.entity.UserRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDto>> create(@Valid @RequestBody CreateBookingRequestDto request) {
        BookingResponseDto response = bookingService.create(request, currentUserId(), currentUserRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<BookingResponseDto>> listMy(
            @RequestParam(defaultValue = "false") boolean all,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return ApiResponse.success(bookingService.listMy(currentUserId(), currentUserRole(), all, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponseDto> getById(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.getById(id, currentUserId(), currentUserRole()));
    }

    @PutMapping("/{id}/accept")
    public ApiResponse<BookingResponseDto> accept(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.accept(id, currentUserId(), currentUserRole()));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<BookingResponseDto> reject(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.reject(id, currentUserId(), currentUserRole()));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<BookingResponseDto> confirm(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.confirm(id, currentUserId(), currentUserRole()));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<BookingResponseDto> cancel(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.cancel(id, currentUserId(), currentUserRole()));
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

    private UserRole currentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .filter(role -> role.startsWith("ROLE_"))
                .findFirst()
                .map(role -> UserRole.valueOf(role.substring("ROLE_".length())))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }
}
