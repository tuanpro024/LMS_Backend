package com.lms.identity.controller;

import org.springframework.web.bind.annotation.*;
import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.dto.request.UpdateProfileRequest;
import com.lms.identity.dto.request.ChangePasswordRequest;
import com.lms.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> me(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(userService.getProfile(principal.userId()));
    }

    @PutMapping("/me")
    public ApiResponse<ProfileResponse> updateMe(Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(userService.updateProfile(principal.userId(), request));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        userService.changePassword(principal.userId(), request);
        return ApiResponse.ok(null);
    }
}
