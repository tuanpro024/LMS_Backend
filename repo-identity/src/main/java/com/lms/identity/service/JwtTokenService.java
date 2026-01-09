package com.lms.identity.service;

import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.entity.User;

public interface JwtTokenService {
    AuthResponse issueTokens(User user);
    AuthResponse refreshTokens(String refreshToken);
}
