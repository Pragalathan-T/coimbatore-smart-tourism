package com.yourorg.tourism.auth.mapper;

import org.springframework.stereotype.Component;

import com.yourorg.tourism.auth.dto.RegisterRequestDto;
import com.yourorg.tourism.auth.dto.RegisterRole;
import com.yourorg.tourism.user.dto.CreateUserCommandDto;
import com.yourorg.tourism.user.entity.UserRole;

@Component
public class AuthMapper {

    public CreateUserCommandDto toCreateUserCommand(RegisterRequestDto request, String passwordHash) {
        return new CreateUserCommandDto(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordHash,
                mapRole(request.role()),
                Boolean.TRUE
        );
    }

    private UserRole mapRole(RegisterRole role) {
        return switch (role) {
            case TOURIST -> UserRole.TOURIST;
            case GUIDE -> UserRole.GUIDE;
        };
    }
}
