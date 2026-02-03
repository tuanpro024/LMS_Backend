package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.identity.dto.response.LoginHistoryResponse;
import com.lms.identity.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/login-history")
@RequiredArgsConstructor
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @GetMapping
    public ApiResponse<List<LoginHistoryResponse>> getMyHistory() {
        return ApiResponse.ok(loginHistoryService.getMyLoginHistory());
    }
}