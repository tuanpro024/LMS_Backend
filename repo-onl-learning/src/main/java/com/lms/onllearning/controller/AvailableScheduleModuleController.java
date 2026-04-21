package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.response.AvailableScheduleModuleResponse;
import com.lms.onllearning.entity.enums.ScheduleModuleType;
import com.lms.onllearning.service.IAvailableScheduleModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Browse các study set có sẵn từ các repo ôn luyện.
 * Chỉ ADMIN / TEACHER_MANAGER mới có quyền truy cập.
 */
@RestController
@RequestMapping("/schedule-available-modules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
public class AvailableScheduleModuleController {

    private final IAvailableScheduleModuleService availableModuleService;

    /**
     * Lấy tất cả study set từ tất cả repo ôn luyện.
     * GET /admin/schedule-available-modules?q=
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailableScheduleModuleResponse>>> getAll(
            @RequestParam(required = false) String q) {

        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAllAvailableModules(q)));
    }

    /**
        * Lọc theo loại module (FLASHCARD, WRITING, KANJI, PRONUNCIATION, QUIZ, LISTENING).
     * GET /admin/schedule-available-modules/{type}?q=
     */
    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<List<AvailableScheduleModuleResponse>>> getByType(
            @PathVariable ScheduleModuleType type,
            @RequestParam(required = false) String q) {

        return ResponseEntity.ok(ApiResponse.ok(availableModuleService.getAvailableModulesByType(type, q)));
    }
}
