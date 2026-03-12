package com.lms.dictionary.config;

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

import java.nio.file.Files;
import java.nio.file.Path;
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
    public RSAPublicKey rsaPublicKey() {
        // Try configured path first, then common local run fallback from
        // repo-dictionary module.
        List<String> candidates = List.of(publicKeyPath, "file:../secrets/jwt-public.pem");

        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            if (candidate.startsWith("file:")) {
                Path filePath = Path.of(candidate.substring("file:".length()));
                if (!Files.exists(filePath)) {
                    continue;
                }
            }
            return RsaKeyUtil.loadPublicKey(candidate);
        }

        throw new IllegalStateException("Cannot load RSA public key. Checked paths: " + candidates);
    }

    @Bean
    public BaseJwtFilter jwtFilter(RSAPublicKey publicKey) {
        List<String> skipPatterns = List.of(
                "/health",
                "/actuator/**");

        // GET endpoints support optional auth (public read access)
        List<String> optionalPatterns = List.of(
                "/vocabularies/**",
                "/word-types/**");

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
                        // Public GET endpoints (read-only)
                        .requestMatchers(HttpMethod.GET, "/vocabularies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/word-types/**").permitAll()
                        // All write operations require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
