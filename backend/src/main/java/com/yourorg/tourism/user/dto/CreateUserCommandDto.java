package com.yourorg.tourism.user.dto;

import com.yourorg.tourism.user.entity.UserRole;

public record CreateUserCommandDto(
        String firstName,
        String lastName,
        String email,
        String passwordHash,
        UserRole role,
        Boolean isActive
) {
}
