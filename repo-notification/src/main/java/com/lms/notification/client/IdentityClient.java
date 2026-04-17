package com.lms.notification.client;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.notification.client.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "repo-identity-api")
public interface IdentityClient {

    @GetMapping("/internal/profiles/{userId}")
    ApiResponse<UserProfileDto> getProfile(@PathVariable("userId") String userId);

    @GetMapping("/internal/profiles/ids")
    ApiResponse<PageResponse<String>> getActiveUserIds(
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
