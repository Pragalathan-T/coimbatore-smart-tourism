package com.yourorg.tourism.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.user.dto.CreateUserCommandDto;
import com.yourorg.tourism.user.dto.UserAuthDto;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.entity.UserEntity;
import com.yourorg.tourism.user.mapper.UserMapper;
import com.yourorg.tourism.user.repository.UserRepository;
import org.springframework.http.HttpStatus;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getByEmail(String email) {
        UserEntity user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserAuthDto getAuthByEmail(String email) {
        UserEntity user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return userMapper.toAuthDto(user);
    }

    @Transactional
    public UserResponseDto create(CreateUserCommandDto command) {
        String normalizedEmail = normalizeEmail(command.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new AppException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Email already exists");
        }

        UserEntity entity = userMapper.toEntity(new CreateUserCommandDto(
                command.firstName(),
                command.lastName(),
                normalizedEmail,
                command.passwordHash(),
                command.role(),
                command.isActive()
        ));
        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
