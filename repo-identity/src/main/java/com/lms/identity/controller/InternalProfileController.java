package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

@RestController
@RequestMapping("/internal/profiles")
@RequiredArgsConstructor
public class InternalProfileController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ApiResponse<ProfileResponse> getProfile(@PathVariable String userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @PostMapping("/batch")
    public ApiResponse<List<ProfileResponse>> getProfilesByIds(@RequestBody List<String> userIds) {
        return ApiResponse.ok(userService.getProfilesByIds(userIds));
    }

    @GetMapping("/ids")
    public ApiResponse<PageResponse<String>> getActiveUserIds(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int size) {
        return ApiResponse.ok(userService.getActiveUserIds(page, size));
    }
}
