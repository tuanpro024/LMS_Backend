package com.lms.onllearning.config;

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

        // Syllabus endpoints are public — optional auth so auth info is set if present
        List<String> optionalPatterns = List.of(
                "/api/onl/syllabuses",
                "/api/onl/syllabuses/**",
                "/api/onl/courses",
                "/api/onl/courses/**");

        return new BaseJwtFilter(publicKey, skipPatterns, optionalPatterns);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BaseJwtFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator & health
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        // Public: xem syllabus (catalog quảng cáo)
                        .requestMatchers(HttpMethod.GET, "/syllabuses/**").permitAll()
                        // Public: xem danh sách & chi tiết khóa học
                        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()
                        // Course management: ADMIN + TEACHER_MANAGER có thể tạo/chỉnh sửa
                        .requestMatchers(HttpMethod.POST, "/courses")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/courses/**")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        // Course delete và syllabus sync chỉ ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        .requestMatchers("/syllabus/**").hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        // Admin/Staff: xem leads, export Excel
                        .requestMatchers(HttpMethod.GET, "/leads/**")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        // Schedule modules: ADMIN/TEACHER_MANAGER quản lý module ôn luyện
                        .requestMatchers(HttpMethod.POST, "/schedule-modules/**")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/schedule-modules/**")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/schedule-modules/**")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        // Browse available study sets: chỉ ADMIN/TEACHER_MANAGER
                        .requestMatchers("/schedule-available-modules/**")
                        .hasAnyRole("ADMIN", "TEACHER_MANAGER")
                        // Authenticated: đăng ký tư vấn
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
