package com.lms.aipractice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "repo-identity-api")
public interface IdentityClient {

    @GetMapping("/internal/premium/{userId}/check")
    boolean isPremium(@PathVariable("userId") String userId);
}
