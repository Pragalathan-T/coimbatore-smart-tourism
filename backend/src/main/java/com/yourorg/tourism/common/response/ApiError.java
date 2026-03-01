package com.yourorg.tourism.common.response;

public record ApiError(
        String errorCode,
        String message
) {
}
