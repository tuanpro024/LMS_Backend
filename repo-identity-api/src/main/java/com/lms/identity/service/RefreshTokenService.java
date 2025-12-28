package com.lms.identity.service;

import com.lms.identity.entity.RefreshToken;

import java.time.Duration;

public interface RefreshTokenService {
    RefreshToken store(String token, String userId, String parentJti, Duration ttl);
    RefreshToken validate(String token);
    void revokeToken(String token);
    void revokeById(String jti);
}
