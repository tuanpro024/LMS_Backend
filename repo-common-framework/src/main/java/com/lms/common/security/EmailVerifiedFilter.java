package com.lms.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Chặn người dùng chưa verify email (emailVerified=false).
 * Chỉ chạy khi đã có Authentication. Có thể bỏ qua các path được cấu hình
 * (public).
 */
public class EmailVerifiedFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(EmailVerifiedFilter.class);
    private final List<String> skipPatterns;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public EmailVerifiedFilter(List<String> skipPatterns) {
        this.skipPatterns = skipPatterns == null ? Collections.emptyList() : skipPatterns;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return skipPatterns.stream().anyMatch(p -> matcher.match(p, path) || path.startsWith(p));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("EmailVerifiedFilter processing path: {}, authentication present: {}",
                path, authentication != null);

        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            log.debug("Checking email verification status for user: {} ({})",
                    principal.userId(), principal.email());

            if (!principal.emailVerified()) {
                log.warn("Access denied - Email not verified for user: {} ({}), path: {}",
                        principal.userId(), principal.email(), path);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"success\":false,\"errorCode\":\"233\",\"errorMessage\":\"Email not verified\"}");
                return;
            }

            log.debug("Email verification check passed for user: {}", principal.userId());
        }

        filterChain.doFilter(request, response);
    }
}
