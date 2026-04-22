package com.lms.videocourse.client;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.client.dto.PaymentAccessCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for repo-payment service.
 * JWT token is forwarded automatically by FeignConfig.
 * Used to verify if the current user has access to a package before allowing review.
 */
@FeignClient(name = "repo-payment")
public interface PaymentClient {

    @GetMapping("/access-check")
    ApiResponse<PaymentAccessCheckResponse> checkAccess(@RequestParam("packageId") String packageId);
}
