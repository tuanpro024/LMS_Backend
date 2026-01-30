package com.lms.learningpath.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Configuration for Feign clients to forward authentication headers
 * and other request context to downstream microservices.
 */
@Configuration
public class FeignConfig {

    /**
     * Interceptor to forward Authorization header from the current request
     * to Feign client requests.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Try to get Authorization header from current HTTP request
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");

                    if (authHeader != null && !authHeader.isEmpty()) {
                        template.header("Authorization", authHeader);
                    }
                }

                // Alternative: get from SecurityContext if available
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && template.headers().get("Authorization") == null) {
                    // If we have authentication but no header was set,
                    // the token might need to be extracted from principal
                    // This is a fallback mechanism
                }
            }
        };
    }
}
