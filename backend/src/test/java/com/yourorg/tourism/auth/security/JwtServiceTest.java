package com.yourorg.tourism.auth.security;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12";
    private static final long EXPIRATION_MS = 86400000L;
    private static final String TEST_ISSUER = "tourism-backend";
    private static final String TEST_AUDIENCE = "tourism-api";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS, TEST_ISSUER, TEST_AUDIENCE);
    }

    @Test
    void generateToken_shouldIncludeSubjectRoleAndTokenVersionClaims() {
        UUID userId = UUID.randomUUID();
        String role = "ADMIN";
        int tokenVersion = 5;

        String token = jwtService.generateToken(userId, role, tokenVersion);

        Claims claims = jwtService.parseAndValidate(token);

        // Verify sub claim
        assertEquals(userId.toString(), claims.getSubject(), "JWT subject should be userId");

        // Verify role claim
        assertEquals(role, claims.get("role", String.class), "JWT role claim should match");

        // Verify tv (tokenVersion) claim
        assertEquals(tokenVersion, claims.get("tv", Integer.class), "JWT tv claim should match tokenVersion");
    }

    @Test
    void generateToken_shouldIncludeIssuerAndAudienceClaims() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "TOURIST", 0);

        Claims claims = jwtService.parseAndValidate(token);

        assertEquals(TEST_ISSUER, claims.getIssuer(), "JWT issuer should match");
        assertTrue(claims.getAudience().contains(TEST_AUDIENCE), "JWT audience should contain configured audience");
    }

    @Test
    void generateToken_shouldIncludeExpirationClaim() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "GUIDE", 1);

        Claims claims = jwtService.parseAndValidate(token);

        assertNotNull(claims.getExpiration(), "JWT should have expiration");
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis(), "Token should not be expired");
    }

    @Test
    void getExpirationMs_shouldReturnConfiguredExpiration() {
        assertEquals(EXPIRATION_MS, jwtService.getExpirationMs());
    }

    @Test
    void generateToken_withDefaultTokenVersion_shouldSetTvToZero() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "TOURIST");

        Claims claims = jwtService.parseAndValidate(token);

        assertEquals(0, claims.get("tv", Integer.class), "Default tokenVersion should be 0");
    }

    @Test
    void extractUserId_shouldReturnCorrectUuid() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "ADMIN", 3);

        UUID extractedId = jwtService.extractUserId(token);

        assertEquals(userId, extractedId);
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        UUID userId = UUID.randomUUID();
        String role = "GUIDE";
        String token = jwtService.generateToken(userId, role, 2);

        String extractedRole = jwtService.extractRole(token);

        assertEquals(role, extractedRole);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "TOURIST", 0);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }
}
