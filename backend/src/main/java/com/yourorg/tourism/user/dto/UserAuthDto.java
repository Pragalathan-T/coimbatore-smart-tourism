package com.yourorg.tourism.user.dto;

import java.util.UUID;

import com.yourorg.tourism.user.entity.UserRole;

public record UserAuthDto(
        UUID id,
        String email,
        String passwordHash,
        UserRole role,
        boolean isActive
) {
}
