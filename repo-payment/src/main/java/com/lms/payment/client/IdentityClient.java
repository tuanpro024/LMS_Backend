package com.lms.payment.client;

import com.lms.common.dto.ApiResponse;
import com.lms.payment.client.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "repo-identity-api")
public interface IdentityClient {

    @GetMapping("/internal/profiles/{userId}")
    ApiResponse<UserProfileDto> getProfile(@PathVariable("userId") String userId);

    @PostMapping("/internal/profiles/batch")
    ApiResponse<List<UserProfileDto>> getProfilesBatch(@RequestBody List<String> userIds);
}
