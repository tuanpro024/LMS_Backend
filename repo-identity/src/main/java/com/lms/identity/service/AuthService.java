package com.lms.identity.service;

import com.lms.identity.dto.request.*;
import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.dto.response.OtpVerificationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request, HttpServletRequest servletRequest);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    String requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void validateResetToken(String token);

    String requestEmailVerification(String email);

    AuthResponse verifyEmail(String token);

    // Mobile Signup with OTP
    OtpVerificationResponse mobileSignup(MobileSignupRequest request);

    AuthResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse verifyDeviceOtp(DeviceVerificationRequest request, HttpServletRequest servletRequest);
    
    String resendDeviceOtp(String email);
}
