package com.yourorg.tourism.auth.security;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.tourism.user.dto.UserAuthDto;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturn401WhenIssuerOrAudienceIsInvalid() throws ServletException, IOException {
        String secret = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12";
        JwtService expectedService = new JwtService(secret, 86400000L, "tourism-backend", "tourism-api");
        JwtService wrongIssuerService = new JwtService(secret, 86400000L, "wrong-issuer", "tourism-api");

        UserService userService = mock(UserService.class);
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(expectedService, userService, objectMapper);

        UUID userId = UUID.randomUUID();
        String token = wrongIssuerService.generateToken(userId, UserRole.TOURIST.name());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/places");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn401WhenAudienceIsInvalid() throws ServletException, IOException {
        String secret = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12";
        JwtService expectedService = new JwtService(secret, 86400000L, "tourism-backend", "tourism-api");
        JwtService wrongAudienceService = new JwtService(secret, 86400000L, "tourism-backend", "wrong-audience");

        UserService userService = mock(UserService.class);
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(expectedService, userService, objectMapper);

        UUID userId = UUID.randomUUID();
        String token = wrongAudienceService.generateToken(userId, UserRole.TOURIST.name());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/places");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn401WhenUserIsInactive() throws ServletException, IOException {
        String secret = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12";
        JwtService jwtService = new JwtService(secret, 86400000L, "tourism-backend", "tourism-api");
        UserService userService = mock(UserService.class);
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userService, objectMapper);

        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, UserRole.GUIDE.name(), 0);
        when(userService.getAuthById(userId))
            .thenReturn(new UserAuthDto(userId, "guide@example.com", "hash", UserRole.GUIDE, false, 0));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/places");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldReturn401TokenStaleWhenTokenVersionDoesNotMatch() throws ServletException, IOException {
        String secret = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12";
        JwtService jwtService = new JwtService(secret, 86400000L, "tourism-backend", "tourism-api");
        UserService userService = mock(UserService.class);
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userService, objectMapper);

        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, UserRole.GUIDE.name(), 0);
        when(userService.getAuthById(userId))
                .thenReturn(new UserAuthDto(userId, "guide@example.com", "hash", UserRole.GUIDE, true, 1));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/places");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("TOKEN_STALE"));
    }
}
