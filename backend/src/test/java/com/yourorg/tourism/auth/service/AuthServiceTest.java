package com.yourorg.tourism.auth.service;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yourorg.tourism.auth.dto.AuthTokenResponseDto;
import com.yourorg.tourism.auth.dto.LoginRequestDto;
import com.yourorg.tourism.auth.dto.RegisterRequestDto;
import com.yourorg.tourism.auth.dto.RegisterRole;
import com.yourorg.tourism.auth.mapper.AuthMapper;
import com.yourorg.tourism.auth.repository.AuthGuideVerificationRepository;
import com.yourorg.tourism.auth.security.JwtService;
import com.yourorg.tourism.user.dto.CreateUserCommandDto;
import com.yourorg.tourism.user.dto.UserAuthDto;
import com.yourorg.tourism.user.dto.UserResponseDto;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthGuideVerificationRepository guideVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMapper authMapper;

    private JwtService jwtService;
    private AuthService authService;

    private static final String TEST_SECRET = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12";
    private static final long EXPIRATION_MS = 86400000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS, "tourism-backend", "tourism-api");
        authService = new AuthService(userService, guideVerificationRepository, passwordEncoder, jwtService, authMapper);
    }

    @Test
    void login_shouldReturnResponseWithAllFields() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "admin@example.com";
        String password = "password123";
        int tokenVersion = 3;

        LoginRequestDto request = new LoginRequestDto(email, password);
        UserAuthDto authUser = new UserAuthDto(userId, email, "hashedPassword", UserRole.ADMIN, true, tokenVersion);

        when(userService.getAuthByEmail(email)).thenReturn(authUser);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

        // When
        AuthTokenResponseDto response = authService.login(request);

        // Then
        assertNotNull(response.token(), "Token should not be null");
        assertEquals("Bearer", response.tokenType());
        assertEquals("ADMIN", response.role(), "Response should include role");
        assertEquals(userId.toString(), response.userId(), "Response should include userId");
        assertEquals(tokenVersion, response.tokenVersion(), "Response should include tokenVersion");
        assertNotNull(response.expiresAtEpochSeconds(), "Response should include expiresAtEpochSeconds");
        assertTrue(response.expiresAtEpochSeconds() > System.currentTimeMillis() / 1000, 
                "Expiration should be in the future");
    }

    @Test
    void login_responseFieldsShouldMatchJwtClaims() {
        // Given
        UUID userId = UUID.randomUUID();
        int tokenVersion = 5;
        UserAuthDto authUser = new UserAuthDto(userId, "guide@example.com", "hash", UserRole.GUIDE, true, tokenVersion);

        when(userService.getAuthByEmail(anyString())).thenReturn(authUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When
        AuthTokenResponseDto response = authService.login(new LoginRequestDto("guide@example.com", "pass"));

        // Then - Verify response fields match what JWT will contain
        assertEquals("GUIDE", response.role());
        assertEquals(userId.toString(), response.userId());
        assertEquals(tokenVersion, response.tokenVersion());

        // Verify JWT claims match response fields
        var claims = jwtService.parseAndValidate(response.token());
        assertEquals(response.userId(), claims.getSubject(), "JWT sub should match response userId");
        assertEquals(response.role(), claims.get("role", String.class), "JWT role should match response role");
        assertEquals(response.tokenVersion(), claims.get("tv", Integer.class), "JWT tv should match response tokenVersion");
    }

    @Test
    void register_shouldReturnResponseWithAllFields() {
        // Given
        UUID userId = UUID.randomUUID();
        int tokenVersion = 0;

        RegisterRequestDto request = new RegisterRequestDto(
                "John", "Doe", "john@example.com", "password123", RegisterRole.TOURIST, null, null
        );

        UserResponseDto createdUser = new UserResponseDto(userId, "John", "Doe", "john@example.com", 
                UserRole.TOURIST, true, java.time.Instant.now());
        UserAuthDto authUser = new UserAuthDto(userId, "john@example.com", "hash", UserRole.TOURIST, true, tokenVersion);

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(authMapper.toCreateUserCommand(any(RegisterRequestDto.class), anyString()))
                .thenReturn(new CreateUserCommandDto("John", "Doe", "john@example.com", "hashedPassword", UserRole.TOURIST, true));
        when(userService.create(any(CreateUserCommandDto.class))).thenReturn(createdUser);
        when(userService.getAuthById(userId)).thenReturn(authUser);

        // When
        AuthTokenResponseDto response = authService.register(request);

        // Then
        assertNotNull(response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("TOURIST", response.role());
        assertEquals(userId.toString(), response.userId());
        assertEquals(tokenVersion, response.tokenVersion());
        assertNotNull(response.expiresAtEpochSeconds());
    }

    @Test
    void login_allRoles_shouldReturnCorrectRoleInResponse() {
        for (UserRole role : UserRole.values()) {
            // Given
            UUID userId = UUID.randomUUID();
            UserAuthDto authUser = new UserAuthDto(userId, "user@example.com", "hash", role, true, 0);

            when(userService.getAuthByEmail(anyString())).thenReturn(authUser);
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // When
            AuthTokenResponseDto response = authService.login(new LoginRequestDto("user@example.com", "pass"));

            // Then
            assertEquals(role.name(), response.role(), "Response role should match for " + role);
        }
    }
}
