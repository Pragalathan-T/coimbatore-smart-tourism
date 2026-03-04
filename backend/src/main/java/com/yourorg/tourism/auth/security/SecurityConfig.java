package com.yourorg.tourism.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("ADMIN", "GUIDE", "TOURIST")
                        .requestMatchers(HttpMethod.POST, "/api/v1/guides/verification/apply").hasRole("GUIDE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/guides/verification/me").hasRole("GUIDE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/verifications/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/verifications/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/places/**").hasAnyRole("ADMIN", "GUIDE", "TOURIST")
                        .requestMatchers("/api/v1/admin/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings").hasRole("TOURIST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/**").hasAnyRole("ADMIN", "GUIDE", "TOURIST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/*/accept").hasRole("GUIDE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/*/reject").hasRole("GUIDE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/*/confirm").hasRole("TOURIST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/*/cancel").hasRole("TOURIST")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
