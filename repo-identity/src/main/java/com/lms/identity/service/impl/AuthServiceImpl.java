package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.dto.request.LoginRequest;
import com.lms.identity.dto.request.SignupRequest;
import com.lms.identity.entity.*;
import com.lms.identity.repository.RoleRepository;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lms.identity.service.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;
    private final OtpService otpService;

    @Transactional
    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.E255, "Email already in use");
        }
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ApiException(ErrorCode.E221, "Role not found data: ROLE_USER"));
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl(request.getAvatarUrl())
                .address(request.getAddress())
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        user.getRoles().add(userRole);
        User saved = userRepository.save(user);
        // generate verification token (for demo return via response header)
        emailVerificationService.createToken(saved);
        return jwtTokenService.issueTokens(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.E238));
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new ApiException(ErrorCode.BAD_REQUEST,
                    "This account is registered with Google. Please login using Google.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.E239);
        }
        if (!user.isEmailVerified()) {
            throw new ApiException(ErrorCode.E233);
        }
        return jwtTokenService.issueTokens(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        return jwtTokenService.refreshTokens(refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Override
    public String requestPasswordReset(String email) {
        return passwordResetService.requestReset(email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
    }

    @Override
    public void validateResetToken(String token) {
        passwordResetService.validate(token);
    }

    @Override
    public String requestEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.E238));
        return emailVerificationService.createToken(user);
    }

    @Override
    public AuthResponse verifyEmail(String token) {
        User user = emailVerificationService.verify(token);
        return jwtTokenService.issueTokens(user);
    }

    @Transactional
    @Override
    public com.lms.identity.dto.response.OtpVerificationResponse mobileSignup(
            com.lms.identity.dto.request.MobileSignupRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.E255, "Email already in use");
        }

        // Get user role
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ApiException(ErrorCode.E221, "Role not found data: ROLE_USER"));

        // Create user with emailVerified = false
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl(request.getAvatarUrl())
                .address(request.getAddress())
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        user.getRoles().add(userRole);
        User saved = userRepository.save(user);

        // Generate and send OTP
        otpService.generateAndSendOtp(saved);

        return com.lms.identity.dto.response.OtpVerificationResponse.builder()
                .message("OTP has been sent to your email.")
                .email(saved.getEmail())
                .expiresInSeconds(300L) // 5 minutes
                .build();
    }

    @Transactional
    @Override
    public AuthResponse verifyOtp(com.lms.identity.dto.request.VerifyOtpRequest request) {
        User user = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return jwtTokenService.issueTokens(user);
    }
}
