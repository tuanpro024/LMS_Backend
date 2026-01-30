package com.lms.learningpath.config;

import com.lms.common.security.BaseJwtFilter;
import com.lms.common.security.RsaKeyUtil;
import com.lms.common.web.RequestContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${security.jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BaseJwtFilter jwtFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(requestContextFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BaseJwtFilter baseJwtFilter(RSAPublicKey jwtPublicKey) {
        return new BaseJwtFilter(
                jwtPublicKey,
                List.of("/health", "/actuator/**"), // Skip patterns
                List.of() // Optional auth paths
        );
    }

    @Bean
    public RSAPublicKey jwtPublicKey() {
        return RsaKeyUtil.loadPublicKey(publicKeyPath);
    }

    @Bean
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }
}