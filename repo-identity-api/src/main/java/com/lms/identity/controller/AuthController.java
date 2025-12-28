package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.dto.request.LoginRequest;
import com.lms.identity.dto.request.RefreshTokenRequest;
import com.lms.identity.dto.request.SignupRequest;
import com.lms.identity.dto.request.ForgotPasswordRequest;
import com.lms.identity.dto.request.ResetPasswordRequest;
import com.lms.identity.dto.request.VerifyEmailRequest;
import com.lms.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.ok(null);
    }

    // Forgot password
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.requestPasswordReset(request.getEmail());
        return ApiResponse.ok(token);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.ok(null);
    }

    @GetMapping("/reset-password/validate")
    public ApiResponse<Void> validateResetToken(@RequestParam("token") String token) {
        authService.validateResetToken(token);
        return ApiResponse.ok(null);
    }

    // Verify Email
    @PostMapping("/verify-email/request")
    public ApiResponse<String> requestVerifyEmail(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.requestEmailVerification(request.getEmail());
        return ApiResponse.ok(token);
    }

    @PostMapping("/verify-email")
    public ApiResponse<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ApiResponse.ok(authService.verifyEmail(request.getToken()));
    }
}
