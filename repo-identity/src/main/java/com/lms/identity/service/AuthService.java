package com.lms.identity.service;

import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.dto.request.LoginRequest;
import com.lms.identity.dto.request.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    String requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void validateResetToken(String token);

    String requestEmailVerification(String email);

    AuthResponse verifyEmail(String token);
}
