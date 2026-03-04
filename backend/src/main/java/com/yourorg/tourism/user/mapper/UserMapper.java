package com.yourorg.tourism.user.mapper;

import org.springframework.stereotype.Component;

import com.yourorg.tourism.user.dto.CreateUserCommandDto;
import com.yourorg.tourism.user.dto.UserAuthDto;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.entity.UserEntity;

import java.util.Objects;

@Component
public class UserMapper {

    public UserEntity toEntity(CreateUserCommandDto dto) {
        UserEntity entity = new UserEntity();
        entity.setFirstName(dto.firstName());
        entity.setLastName(dto.lastName());
        entity.setEmail(dto.email().trim().toLowerCase());
        entity.setPasswordHash(dto.passwordHash());
        entity.setRole(dto.role());
        entity.setIsActive(dto.isActive() == null ? Boolean.TRUE : dto.isActive());
        return entity;
    }

    public UserResponseDto toResponse(UserEntity entity) {
        return new UserResponseDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRole(),
                Boolean.TRUE.equals(entity.getIsActive()),
                entity.getCreatedAt()
        );
    }

    public UserAuthDto toAuthDto(UserEntity entity) {
        return new UserAuthDto(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                Boolean.TRUE.equals(entity.getIsActive()),
                Objects.requireNonNullElse(entity.getTokenVersion(), 0)
        );
    }
}
