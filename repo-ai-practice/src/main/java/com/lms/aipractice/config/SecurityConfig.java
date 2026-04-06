package com.lms.aipractice.config;

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

    @Value("${security.jwt.public-key-path:file:secrets/jwt-public.pem}")
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

        // Public read-only endpoints (browse study sets, packages, folders)
        List<String> optionalPatterns = List.of(
                "/study-sets/**",
                "/packages/**",
                "/folders/**",
                "/ai-items/**");

        return new BaseJwtFilter(publicKey, skipPatterns, optionalPatterns);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BaseJwtFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health / actuator — always public
                        .requestMatchers("/health", "/actuator/**").permitAll()

                        // Public GET for hierarchy & items
                        .requestMatchers(HttpMethod.GET, "/packages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/folders/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/study-sets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ai-items/**").permitAll()

                        // All other requests require authentication;
                        // fine-grained role checks done via @PreAuthorize on each method
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
