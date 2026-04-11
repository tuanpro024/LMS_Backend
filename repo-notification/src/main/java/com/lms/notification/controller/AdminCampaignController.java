package com.lms.notification.controller;

import com.lms.common.dto.PageResponse;
import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.notification.dto.request.SendCampaignRequest;
import com.lms.notification.dto.response.CampaignResponse;
import com.lms.notification.service.CampaignService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final CampaignService campaignService;

    @PostMapping("/send-now")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampaignResponse> sendNow(
            Authentication authentication,
            @Valid @RequestBody SendCampaignRequest request) {
        
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        CampaignResponse response = campaignService.sendNow(request, principal.userId());
        return ApiResponse.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER_MANAGER')")
    public ApiResponse<PageResponse<CampaignResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        return ApiResponse.ok(campaignService.listCampaigns(page, size));
    }
}
