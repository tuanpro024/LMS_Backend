package com.lms.listening.config;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
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
                "/api/listening-practice/packages",
                "/api/listening-practice/folders",
                "/api/listening-practice/study-sets");

        return new BaseJwtFilter(publicKey, skipPatterns, optionalPatterns);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BaseJwtFilter jwtFilter) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable) // Disable CORS as it is handled by the Gateway
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health and actuator endpoints
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/api/listening-practice/packages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listening-practice/folders/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listening-practice/study-sets/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
