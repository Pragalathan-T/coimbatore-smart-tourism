package com.yourorg.tourism.auth.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourorg.tourism.auth.dto.AuthTokenResponseDto;
import com.yourorg.tourism.auth.dto.LoginRequestDto;
import com.yourorg.tourism.auth.dto.RegisterRequestDto;
import com.yourorg.tourism.auth.dto.RegisterRole;
import com.yourorg.tourism.auth.entity.GuideVerificationEntity;
import com.yourorg.tourism.auth.mapper.AuthMapper;
import com.yourorg.tourism.auth.repository.AuthGuideVerificationRepository;
import com.yourorg.tourism.auth.security.JwtService;
import com.yourorg.tourism.common.exception.AppException;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.user.dto.UserAuthDto;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.service.UserService;

@Service
public class AuthService {

    private final UserService userService;
    private final AuthGuideVerificationRepository guideVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public AuthService(
            UserService userService,
            AuthGuideVerificationRepository guideVerificationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMapper authMapper
    ) {
        this.userService = userService;
        this.guideVerificationRepository = guideVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthTokenResponseDto register(RegisterRequestDto request) {
        if (request.role() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Role is required");
        }

        if (request.role() == RegisterRole.GUIDE) {
            validateGuideVerificationInput(request.idProofUrl(), request.selfieUrl());
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        UserResponseDto createdUser = userService.create(authMapper.toCreateUserCommand(request, hashedPassword));

        if (request.role() == RegisterRole.GUIDE) {
            GuideVerificationEntity verification = new GuideVerificationEntity();
            verification.setUserId(createdUser.id());
            verification.setIdProofUrl(request.idProofUrl().trim());
            verification.setSelfieUrl(request.selfieUrl().trim());
            verification.setVerificationStatus("PENDING");
            guideVerificationRepository.save(verification);
        }

        UserAuthDto authUser = userService.getAuthById(createdUser.id());
        String token = jwtService.generateToken(authUser.id(), authUser.role().name(), authUser.tokenVersion());
        long expiresAtEpoch = Instant.now().plusMillis(jwtService.getExpirationMs()).getEpochSecond();
        return AuthTokenResponseDto.of(
                token,
                "Bearer",
                authUser.role().name(),
                authUser.id(),
                authUser.tokenVersion(),
                expiresAtEpoch
        );
    }

    @Transactional(readOnly = true)
    public AuthTokenResponseDto login(LoginRequestDto request) {
        UserAuthDto authUser = userService.getAuthByEmail(request.email());

        if (!authUser.isActive()) {
            throw new AppException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "User is inactive");
        }

        if (!passwordEncoder.matches(request.password(), authUser.passwordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(authUser.id(), authUser.role().name(), authUser.tokenVersion());
        long expiresAtEpoch = Instant.now().plusMillis(jwtService.getExpirationMs()).getEpochSecond();
        return AuthTokenResponseDto.of(
                token,
                "Bearer",
                authUser.role().name(),
                authUser.id(),
                authUser.tokenVersion(),
                expiresAtEpoch
        );
    }

    private void validateGuideVerificationInput(String idProofUrl, String selfieUrl) {
        if (idProofUrl == null || idProofUrl.isBlank() || selfieUrl == null || selfieUrl.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "idProofUrl and selfieUrl are required for GUIDE registration"
            );
        }
    }
}
