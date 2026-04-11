package com.lms.notification.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.notification.dto.request.SendCampaignRequest;
import com.lms.notification.dto.response.CampaignResponse;
import com.lms.notification.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
