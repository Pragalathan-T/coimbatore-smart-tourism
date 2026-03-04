package com.yourorg.tourism.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final String jwtIssuer;
    private final String jwtAudience;

    public JwtService(
            @Value("${app.security.jwt.secret:abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12}") String jwtSecret,
            @Value("${app.security.jwt.expiration-ms:86400000}") long jwtExpirationMs,
            @Value("${app.security.jwt.issuer:tourism-backend}") String jwtIssuer,
            @Value("${app.security.jwt.audience:tourism-api}") String jwtAudience
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtIssuer = jwtIssuer;
        this.jwtAudience = jwtAudience;
    }

    public String generateToken(UUID userId, String role) {
        return generateToken(userId, role, 0);
    }

    public String generateToken(UUID userId, String role, int tokenVersion) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("tv", tokenVersion)
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseAndValidate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!jwtIssuer.equals(claims.getIssuer())) {
            throw new IllegalArgumentException("Invalid token issuer");
        }

        if (!hasExpectedAudience(claims)) {
            throw new IllegalArgumentException("Invalid token audience");
        }

        return claims;
    }

    public UUID extractUserId(String token) {
        String subject = parseAndValidate(token).getSubject();
        return UUID.fromString(subject);
    }

    public String extractRole(String token) {
        return parseAndValidate(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseAndValidate(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean hasExpectedAudience(Claims claims) {
        Object audienceClaim = claims.get("aud");
        if (audienceClaim instanceof String audience) {
            return jwtAudience.equals(audience);
        }
        if (audienceClaim instanceof Collection<?> audiences) {
            return audiences.contains(jwtAudience);
        }
        return false;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
