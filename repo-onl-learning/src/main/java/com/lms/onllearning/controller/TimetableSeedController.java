package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Endpoint DEV-ONLY: seed mock timetable vào Redis để test fallback khi CMS down.
 * Chỉ active khi chạy với profile "dev" hoặc "local".
 *
 * POST /dev/timetable/seed?email=xxx@gmail.com
 */
@RestController
@RequestMapping("/dev/timetable")
@RequiredArgsConstructor
public class TimetableSeedController {

    private static final String CACHE_KEY_TIMETABLE = "timetable:student:";
    private static final long SEED_TTL_SEC = 3600L; // 1 tiếng

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seedTimetable(
            @RequestParam String email) {

        List<StudentTimetableItemResponse> mockData = buildMockTimetable();

        String cacheKey = CACHE_KEY_TIMETABLE + email;
        redisTemplate.opsForValue().set(cacheKey, mockData, SEED_TTL_SEC, TimeUnit.SECONDS);

        return ResponseEntity.ok(ApiResponse.ok(
                "Đã seed " + mockData.size() + " buổi học vào Redis với key: " + cacheKey
                + " (TTL: " + SEED_TTL_SEC + "s)"
        ));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearTimetable(
            @RequestParam String email) {

        String cacheKey = CACHE_KEY_TIMETABLE + email;
        Boolean deleted = redisTemplate.delete(cacheKey);

        return ResponseEntity.ok(ApiResponse.ok(
                deleted != null && deleted ? "Đã xóa cache: " + cacheKey : "Không tìm thấy key: " + cacheKey
        ));
    }

    private List<StudentTimetableItemResponse> buildMockTimetable() {
        return List.of(
                new StudentTimetableItemResponse(
                        "MOCK-SESSION-001",
                        "MOCK-CLASS-001",
                        null,
                        "DEMO1",
                        "DEMO1",
                        1,
                        "Buổi 1",
                        "2026-03-04T17:00:00.000Z",
                        "2026-03-04T17:00:00.000Z",
                        "2026-03-05T00:00:00.000Z",
                        "2026-03-05T01:30:00.000Z",
                        "COMPLETED",
                        null,
                        "Trần Quyết Tiến",
                        "Trần Quyết Tiến",
                        "https://meet.google.com/mock-room",
                        "PRESENT",
                        "2026-03-05T16:00:00.000Z"
                ),
                new StudentTimetableItemResponse(
                        "MOCK-SESSION-002",
                        "MOCK-CLASS-001",
                        null,
                        "DEMO1",
                        "DEMO1",
                        2,
                        "Buổi 2",
                        "2026-03-05T17:00:00.000Z",
                        "2026-03-05T17:00:00.000Z",
                        "2026-03-06T00:00:00.000Z",
                        "2026-03-06T01:30:00.000Z",
                        "COMPLETED",
                        null,
                        "Trần Quyết Tiến",
                        "Trần Quyết Tiến",
                        "https://meet.google.com/mock-room",
                        "EXCUSED",
                        null
                ),
                new StudentTimetableItemResponse(
                        "MOCK-SESSION-003",
                        "MOCK-CLASS-001",
                        null,
                        "DEMO1",
                        "DEMO1",
                        3,
                        "Buổi 3",
                        "2026-03-10T17:00:00.000Z",
                        "2026-03-10T17:00:00.000Z",
                        "2026-03-11T00:00:00.000Z",
                        "2026-03-11T01:30:00.000Z",
                        "UPCOMING",
                        null,
                        "Trần Quyết Tiến",
                        "Trần Quyết Tiến",
                        "https://meet.google.com/mock-room",
                        null,
                        null
                )
        );
    }
}
