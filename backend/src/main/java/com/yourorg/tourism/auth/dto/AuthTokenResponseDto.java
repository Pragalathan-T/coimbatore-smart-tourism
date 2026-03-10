package com.yourorg.tourism.auth.dto;

import java.util.UUID;

/**
 * Authentication response containing token and explicit user info.
 * The explicit fields mirror JWT claims for frontend convenience,
 * but JWT claims remain the authoritative source for security validation.
 */
public record AuthTokenResponseDto(
        String token,
        String tokenType,
        String role,
        String userId,
        Integer tokenVersion,
        Long expiresAtEpochSeconds
) {
    /**
     * Backward-compatible constructor for minimal response.
     */
    public AuthTokenResponseDto(String token, String tokenType) {
        this(token, tokenType, null, null, null, null);
    }

    /**
     * Full constructor with UUID userId.
     */
    public static AuthTokenResponseDto of(
            String token,
            String tokenType,
            String role,
            UUID userId,
            int tokenVersion,
            long expiresAtEpochSeconds
    ) {
        return new AuthTokenResponseDto(
                token, tokenType, role,
                userId != null ? userId.toString() : null,
                tokenVersion,
                expiresAtEpochSeconds
        );
    }
}
