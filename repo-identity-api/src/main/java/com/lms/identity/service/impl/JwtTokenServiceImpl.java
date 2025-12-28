package com.lms.identity.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.security.JwtUtils;
import com.lms.identity.service.JwtTokenService;
import com.lms.identity.service.RefreshTokenService;
import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;
import com.lms.identity.repository.UserRepository;

import java.time.Duration;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RSAPrivateKey jwtPrivateKey;
    private final RSAPublicKey jwtPublicKey;
    @Value("${security.jwt.issuer:repo-identity}")
    private String issuer;
    @Value("${security.jwt.access-ttl:PT5M}")
    private Duration accessTtl;
    @Value("${security.jwt.refresh-ttl:P7D}")
    private Duration refreshTtl;

    @Override
    public AuthResponse issueTokens(User user) {
        Set<String> roles = extractRoleNames(user);
        String userId = user.getId();
        String access = JwtUtils.generateAccessToken(userId, user.getEmail(), roles, user.isEmailVerified(), accessTtl, issuer, jwtPrivateKey);
        String refresh = JwtUtils.generateRefreshToken(userId, refreshTtl, issuer, jwtPrivateKey, null);
        refreshTokenService.store(refresh, userId, null, refreshTtl);
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .expiresInSeconds(accessTtl.toSeconds())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    @Override
    public AuthResponse refreshTokens(String refreshToken) {
        JwtUtils.JwtPayload payload = JwtUtils.verify(refreshToken, jwtPublicKey);
        if (!"refresh".equals(String.valueOf(payload.type()))) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid refresh token type");
        }
        refreshTokenService.validate(refreshToken);

        String userId = payload.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "User not found"));
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ApiException(ErrorCode.FORBIDDEN, "User is blocked");
        }
        if (!user.isEmailVerified()) {
            throw new ApiException(ErrorCode.E233, "Email not verified");
        }
        Set<String> roles = extractRoleNames(user);
        String access = JwtUtils.generateAccessToken(userId, user.getEmail(), roles, user.isEmailVerified(), accessTtl, issuer, jwtPrivateKey);
        String rotated = JwtUtils.generateRefreshToken(userId, refreshTtl, issuer, jwtPrivateKey, payload.jti());

        refreshTokenService.revokeById(payload.jti());
        refreshTokenService.store(rotated, userId, payload.jti(), refreshTtl);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(rotated)
                .expiresInSeconds(accessTtl.toSeconds())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    private Set<String> extractRoleNames(User user) {
        return user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }
}
