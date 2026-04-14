package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
