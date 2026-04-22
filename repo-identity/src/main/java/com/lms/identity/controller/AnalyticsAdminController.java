package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.identity.repository.UserInteractionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin-only endpoints for viewing interaction analytics.
 * Provides aggregated statistics for the admin dashboard.
 */
@RestController
@RequestMapping("/analytics/admin")
@RequiredArgsConstructor
public class AnalyticsAdminController {

    private final UserInteractionLogRepository repository;

    /**
     * Returns an overview of interaction statistics for a given period.
     * Default: last 7 days.
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<Map<String, Object>> getOverview(
            @RequestParam(defaultValue = "7") int days) {

        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant to = Instant.now();

        long totalEvents = repository.countByTimestampBetween(from, to);
        long activeUsers = repository.countDistinctUsersBetween(from, to);

        // Event type breakdown
        List<Object[]> eventTypeCounts = repository.countByEventTypeBetween(from, to);
        List<Map<String, Object>> eventBreakdown = eventTypeCounts.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("eventType", row[0] != null ? row[0].toString() : "UNKNOWN");
                    map.put("count", row[1]);
                    return map;
                })
                .collect(Collectors.toList());

        // Module breakdown
        List<Object[]> moduleCounts = repository.countByModuleBetween(from, to);
        List<Map<String, Object>> moduleBreakdown = moduleCounts.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("module", row[0] != null ? row[0].toString() : "unknown");
                    map.put("count", row[1]);
                    return map;
                })
                .collect(Collectors.toList());

        // Daily trend
        List<Object[]> dailyCounts = repository.countByDayBetween(from, to);
        List<Map<String, Object>> dailyTrend = dailyCounts.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", row[0] != null ? row[0].toString() : "");
                    map.put("count", row[1]);
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", Map.of("from", from.toString(), "to", to.toString(), "days", days));
        result.put("totalEvents", totalEvents);
        result.put("activeUsers", activeUsers);
        result.put("eventBreakdown", eventBreakdown);
        result.put("moduleBreakdown", moduleBreakdown);
        result.put("dailyTrend", dailyTrend);

        return ApiResponse.ok(result);
    }
}
