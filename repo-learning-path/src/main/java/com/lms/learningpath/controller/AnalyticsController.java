package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.entity.LearningEvent;
import com.lms.learningpath.entity.enums.EventType;
import com.lms.learningpath.repository.LearningEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final LearningEventRepository eventRepository;

    /**
     * Get learning summary for current user
     */
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        String userId = principal.userId();

        // Default to last 30 days
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Get events in period
        List<LearningEvent> events = eventRepository.findByUserIdAndOccurredAtBetween(userId, start, end);

        // Calculate statistics
        long totalModulesCompleted = eventRepository.countByUserIdAndEventTypeAndOccurredAtBetween(
                userId, EventType.MODULE_COMPLETED, start, end
        );

        long totalSetsCompleted = eventRepository.countByUserIdAndEventTypeAndOccurredAtBetween(
                userId, EventType.SET_COMPLETED, start, end
        );

        Long totalDurationSeconds = eventRepository.sumDurationByUserAndPeriod(userId, start, end);

        // Average score
        double averageScore = events.stream()
                .filter(e -> e.getScore() != null && e.getEventType() == EventType.MODULE_COMPLETED)
                .mapToInt(LearningEvent::getScore)
                .average()
                .orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", Map.of("start", startDate, "end", endDate));
        summary.put("totalModulesCompleted", totalModulesCompleted);
        summary.put("totalSetsCompleted", totalSetsCompleted);
        summary.put("totalStudyMinutes", totalDurationSeconds != null ? totalDurationSeconds / 60 : 0);
        summary.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        summary.put("totalEvents", events.size());

        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    /**
     * Get learning events (paginated)
     */
    @GetMapping("/me/events")
    public ResponseEntity<ApiResponse<Page<LearningEvent>>> getMyEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);
        Page<LearningEvent> events = eventRepository.findByUserIdOrderByOccurredAtDesc(
                principal.userId(),
                pageable
        );

        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    /**
     * Get event type breakdown
     */
    @GetMapping("/me/event-breakdown")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getEventBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Object[]> results = eventRepository.countEventTypesByUserAndPeriod(
                principal.userId(),
                start,
                end
        );

        Map<String, Long> breakdown = new HashMap<>();
        for (Object[] result : results) {
            EventType eventType = (EventType) result[0];
            Long count = (Long) result[1];
            breakdown.put(eventType.name(), count);
        }

        return ResponseEntity.ok(ApiResponse.ok(breakdown));
    }
}