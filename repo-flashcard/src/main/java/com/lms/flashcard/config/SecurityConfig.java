package com.lms.flashcard.config;

import com.lms.common.security.BaseJwtFilter;
import com.lms.common.security.RsaKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${security.jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        return RsaKeyUtil.loadPublicKey(publicKeyPath);
    }

    @Bean
    public BaseJwtFilter jwtFilter(RSAPublicKey publicKey) {
        List<String> skipPatterns = List.of(
                "/health",
                "/actuator/**");

        List<String> optionalPatterns = List.of(
                "/api/study-sets",
                "/api/folders");

        return new BaseJwtFilter(publicKey, skipPatterns, optionalPatterns);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BaseJwtFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health and actuator endpoints
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        // Progress endpoint must be authenticated
                        .requestMatchers(HttpMethod.GET, "/api/study-sets/*/progress").authenticated()
                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/api/study-sets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/folders/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
