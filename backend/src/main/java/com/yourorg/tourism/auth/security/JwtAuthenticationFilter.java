package com.yourorg.tourism.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.tourism.common.exception.ErrorCode;
import com.yourorg.tourism.common.response.ApiResponse;
import com.yourorg.tourism.user.dto.UserAuthDto;
import com.yourorg.tourism.user.entity.UserRole;
import com.yourorg.tourism.user.service.UserService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<RequestMatcher> EXCLUDED_PATH_MATCHERS = List.of(
            new AntPathRequestMatcher("/api/v1/auth/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/swagger-ui.html")
    );

    private final JwtService jwtService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return EXCLUDED_PATH_MATCHERS.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = jwtService.parseAndValidate(token);
        } catch (Exception ex) {
            writeUnauthorized(response, "Unauthorized");
            return;
        }

        UUID userId;
        String tokenRole;
        Integer tokenVersion;
        try {
            userId = UUID.fromString(claims.getSubject());
            tokenRole = claims.get("role", String.class);
            tokenVersion = claims.get("tv", Integer.class);
            if (!StringUtils.hasText(tokenRole)) {
                writeUnauthorized(response, "Unauthorized");
                return;
            }
        } catch (Exception ex) {
            writeUnauthorized(response, "Unauthorized");
            return;
        }

        UserAuthDto user;
        try {
            user = userService.getAuthById(userId);
        } catch (Exception ex) {
            writeUnauthorized(response, "Unauthorized");
            return;
        }

        if (!user.isActive()) {
            writeUnauthorized(response, "Unauthorized");
            return;
        }

        if (!user.role().name().equals(tokenRole)) {
            writeForbidden(response, "Role changed");
            return;
        }

        if (tokenVersion == null || !tokenVersion.equals(user.tokenVersion())) {
            writeUnauthorized(response, ErrorCode.TOKEN_STALE, "Token stale. Please login again.");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + UserRole.valueOf(tokenRole).name()))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeUnauthorized(response, ErrorCode.UNAUTHORIZED, message);
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(errorCode.name(), message));
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(ErrorCode.FORBIDDEN.name(), message));
    }
}
