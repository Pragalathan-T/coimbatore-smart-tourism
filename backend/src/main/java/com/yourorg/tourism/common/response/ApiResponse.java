package com.yourorg.tourism.common.response;

import java.time.Instant;

public record ApiResponse<T>(
        String status,
        T data,
        String message,
        String errorCode,
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", data, "Success", null, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("SUCCESS", data, message, null, Instant.now());
    }

    public static ApiResponse<Void> error(String errorCode, String message) {
        return new ApiResponse<>("ERROR", null, message, errorCode, Instant.now());
    }
}
