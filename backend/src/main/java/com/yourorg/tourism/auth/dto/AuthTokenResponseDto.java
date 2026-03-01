package com.yourorg.tourism.auth.dto;

public record AuthTokenResponseDto(
        String token,
        String tokenType
) {
}
