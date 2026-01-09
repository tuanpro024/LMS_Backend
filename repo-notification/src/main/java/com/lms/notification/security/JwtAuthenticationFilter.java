package com.lms.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import com.lms.common.exception.ApiException;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.JwtUtils;
import com.lms.common.security.SecurityConstants;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RSAPublicKey jwtPublicKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        if (header == null || !header.startsWith(SecurityConstants.HEADER_BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(SecurityConstants.HEADER_BEARER_PREFIX.length()).trim();
        try {
            JwtUtils.JwtPayload payload = JwtUtils.verify(token, jwtPublicKey);
            AuthPrincipal principal = new AuthPrincipal(payload.userId(), payload.email(), payload.emailVerified(), payload.roles());
            List<SimpleGrantedAuthority> authorities = payload.roles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ApiException ex) {
            // ignore, unauthenticated will be blocked by security config
        }
        filterChain.doFilter(request, response);
    }
}
