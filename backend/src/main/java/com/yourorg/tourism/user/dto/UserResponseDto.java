package com.yourorg.tourism.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.yourorg.tourism.user.entity.UserRole;

public record UserResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        boolean isActive,
        Instant createdAt
) {
}
