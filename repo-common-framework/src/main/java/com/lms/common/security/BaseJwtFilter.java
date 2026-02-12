package com.lms.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.lms.common.exception.ApiException;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JWT filter dùng chung:
 * - skip các path cấu hình (không chạy filter).
 * - optional paths: nếu có token hợp lệ thì set Authentication, nếu không
 * có/không hợp lệ thì bỏ qua.
 * - các path còn lại: cố gắng parse token; nếu token hợp lệ thì set
 * Authentication, token sai -> chỉ log và bỏ qua
 * (quyền truy cập cuối cùng do cấu hình security quyết định).
 */
public class BaseJwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BaseJwtFilter.class);
    private final RSAPublicKey publicKey;
    private final List<String> skipPatterns;
    private final List<String> optionalPatterns;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public BaseJwtFilter(RSAPublicKey publicKey, List<String> skipPatterns, List<String> optionalPatterns) {
        this.publicKey = publicKey;
        this.skipPatterns = skipPatterns == null ? Collections.emptyList() : skipPatterns;
        this.optionalPatterns = optionalPatterns == null ? Collections.emptyList() : optionalPatterns;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return matches(path, skipPatterns);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String header = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        boolean isOptional = matches(path, optionalPatterns);

        log.debug("Processing authentication for path: {}, token present: {}, optional: {}",
                path, header != null && header.startsWith(SecurityConstants.HEADER_BEARER_PREFIX), isOptional);

        if (header == null || !header.startsWith(SecurityConstants.HEADER_BEARER_PREFIX)) {
            log.debug("No Bearer token found in request to {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(SecurityConstants.HEADER_BEARER_PREFIX.length()).trim();
        try {
            log.debug("Attempting to verify JWT token for path: {}", path);
            JwtUtils.JwtPayload payload = JwtUtils.verify(token, publicKey);

            AuthPrincipal principal = new AuthPrincipal(payload.userId(), payload.email(), payload.emailVerified(),
                    payload.roles());
            List<SimpleGrantedAuthority> authorities = payload.roles().stream()
                    .filter(Objects::nonNull)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
                    null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("JWT authenticated user: {} ({}), roles: {}", payload.userId(), payload.email(), payload.roles());
            log.debug("SecurityContext updated with authentication for path: {}", path);
        } catch (ApiException ex) {
            if (!isOptional) {
                log.warn("JWT verification failed on path {}: {} - {}", path, ex.getErrorCode(), ex.getMessage(), ex);
            } else {
                log.debug("JWT verification failed on optional path {}: {}", path, ex.getMessage());
            }
            // optional: hoặc token sai -> không set auth, cho qua; access sẽ do Security
            // config quyết định.
        }
        filterChain.doFilter(request, response);
    }

    private boolean matches(String path, List<String> patterns) {
        return patterns.stream().anyMatch(p -> matcher.match(p, path) || path.startsWith(p));
    }
}
