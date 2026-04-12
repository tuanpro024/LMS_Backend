package com.lms.videocourse.config;

import com.lms.common.security.BaseJwtFilter;
import com.lms.common.security.RsaKeyUtil;
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
                "/actuator/**",
                "/health");

        List<String> optionalPatterns = List.of(
                "/video-courses/**", // Public GET
                "/video-steps/**", // Public GET
                "/video-modules/**", // Public GET
                "/packages/**", // Public GET
                "/folders/**", // Public GET
                "/study-sets/**"); // Public GET

        return new BaseJwtFilter(publicKey, skipPatterns, optionalPatterns);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BaseJwtFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permit OPTIONS requests for CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Actuator
                        .requestMatchers("/actuator/**", "/health").permitAll()

                        // Public GET access to course/step/module information
                        .requestMatchers(HttpMethod.GET, "/video-courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/video-steps/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/video-modules/**").permitAll()

                        // Public GET for organizational hierarchy
                        .requestMatchers(HttpMethod.GET, "/packages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/folders/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/study-sets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/syllabus/**").permitAll()

                        // Admin/Teacher only endpoints
                        .requestMatchers("/admin/**").authenticated()

                        // Write operations require authentication
                        .requestMatchers(HttpMethod.POST, "/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/**").authenticated()

                        // Progress tracking requires authentication
                        .requestMatchers("/progress/**").authenticated()

                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
