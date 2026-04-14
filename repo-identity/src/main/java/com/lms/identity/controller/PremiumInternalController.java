package com.lms.identity.controller;

import com.lms.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/premium")
@RequiredArgsConstructor
public class PremiumInternalController {

    private final UserService userService;

    @GetMapping("/{userId}/check")
    public boolean checkPremium(@PathVariable String userId) {
        return userService.isPremium(userId);
    }
}
