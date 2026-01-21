package com.lms.writing.config;

import com.lms.common.security.BaseJwtFilter;
import com.lms.common.security.RsaKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                "/study-sets",
                "/folders",
                "/packages",
                "/subjects",
                "/slots",
                "/words");

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
                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/study-sets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/folders/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/packages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/subjects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/slots/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/words/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
