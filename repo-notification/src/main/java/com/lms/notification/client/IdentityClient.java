package com.lms.notification.client;

import com.lms.common.dto.ApiResponse;
import com.lms.notification.client.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "repo-identity-api")
public interface IdentityClient {

    @GetMapping("/internal/profiles/{userId}")
    ApiResponse<UserProfileDto> getProfile(@PathVariable("userId") String userId);
}
