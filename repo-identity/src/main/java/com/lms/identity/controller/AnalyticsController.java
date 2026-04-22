package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.InteractionLogRequest;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.service.InteractionLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for receiving user interaction events.
 * Only processes events for authenticated users with ROLE_USER (students).
 */
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final InteractionLogService interactionLogService;

    /**
     * Receives a batch of interaction events from the frontend.
     * This is the primary endpoint — frontend accumulates events and sends them periodically.
     */
    @PostMapping("/interactions")
    public ApiResponse<Void> logInteractions(
            @RequestBody List<InteractionLogRequest> events,
            @AuthenticationPrincipal AuthPrincipal principal,
            HttpServletRequest httpRequest) {

        if (principal == null) {
            return ApiResponse.error("UNAUTHENTICATED", "User not authenticated");
        }

        // Only log student interactions
        if (!principal.hasRole("ROLE_USER")) {
            return ApiResponse.ok(null);
        }

        String userId = principal.userId();
        String userRole = "ROLE_USER";
        String ipAddress = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        for (InteractionLogRequest event : events) {
            InteractionLogRequest enriched = ensureTimestamp(event);
            interactionLogService.logInteraction(enriched, userId, userRole, ipAddress, userAgent);
        }

        return ApiResponse.ok(null);
    }

    /**
     * Receives a single interaction event.
     * Useful for critical events that should be sent immediately (e.g. payment_success).
     */
    @PostMapping("/interaction")
    public ApiResponse<Void> logInteraction(
            @RequestBody InteractionLogRequest event,
            @AuthenticationPrincipal AuthPrincipal principal,
            HttpServletRequest httpRequest) {

        if (principal == null) {
            return ApiResponse.error("UNAUTHENTICATED", "User not authenticated");
        }

        if (!principal.hasRole("ROLE_USER")) {
            return ApiResponse.ok(null);
        }

        String userId = principal.userId();
        String userRole = "ROLE_USER";
        String ipAddress = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        InteractionLogRequest enriched = ensureTimestamp(event);
        interactionLogService.logInteraction(enriched, userId, userRole, ipAddress, userAgent);

        return ApiResponse.ok(null);
    }

    private InteractionLogRequest ensureTimestamp(InteractionLogRequest event) {
        if (event.timestamp() != null) return event;
        return new InteractionLogRequest(
                event.sessionId(),
                event.eventType(),
                Instant.now(),
                event.module(),
                event.targetId(),
                event.pageUrl(),
                event.metadata()
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
