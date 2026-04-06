package com.lms.notification.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.notification.dto.response.NotificationPageResponse;
import com.lms.notification.dto.response.NotificationResponse;
import com.lms.notification.service.NotificationService;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<NotificationPageResponse> list(Authentication authentication,
                                                      @RequestParam(name = "readStatus", required = false) String readStatus,
                                                      @RequestParam(name = "fromDate", required = false) java.time.Instant fromDate,
                                                      @RequestParam(name = "toDate", required = false) java.time.Instant toDate,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) int size) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        String userId = principal.userId();
        return ApiResponse.ok(notificationService.list(userId, readStatus, fromDate, toDate, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(notificationService.unreadCount(principal.userId()));
    }

    @PostMapping("/{id}/seen")
    public ApiResponse<NotificationResponse> markSeen(Authentication authentication,
                                                      @PathVariable("id") String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(notificationService.markSeen(principal.userId(), id));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(Authentication authentication,
                                                      @PathVariable("id") String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(notificationService.markRead(principal.userId(), id));
    }

    @PostMapping("/seen/all")
    public ApiResponse<Void> markAllSeen(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        notificationService.markAllSeen(principal.userId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/read/all")
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        notificationService.markAllRead(principal.userId());
        return ApiResponse.ok(null);
    }
}
