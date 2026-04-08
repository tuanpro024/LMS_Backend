package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.request.AddModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ImportModuleToScheduleRequest;
import com.lms.onllearning.dto.request.ReorderScheduleModulesRequest;
import com.lms.onllearning.dto.response.ScheduleModuleResponse;
import com.lms.onllearning.service.IScheduleModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Quản lý module ôn luyện trong các buổi học của syllabus.
 * - ADMIN / TEACHER_MANAGER: thêm, xoá, sắp xếp lại module.
 * - Tất cả người dùng đã xác thực: xem danh sách module của buổi học.
 */
@RestController
@RequestMapping("/schedule-modules")
@RequiredArgsConstructor
public class ScheduleModuleController {

    private final IScheduleModuleService scheduleModuleService;

    /**
     * Thêm module đã có sẵn (chọn study set từ repo ngoài).
     * POST /schedule-modules
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleModuleResponse>> addModule(
            @RequestBody @Valid AddModuleToScheduleRequest request) {

        ScheduleModuleResponse response = scheduleModuleService.addModule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Import Excel để tạo nội dung mới và gắn module vào buổi học.
     * POST /schedule-modules/import  (multipart/form-data)
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ScheduleModuleResponse>> importModule(
            @RequestPart("request") @Valid ImportModuleToScheduleRequest request,
            @RequestPart("file") MultipartFile file) {

        ScheduleModuleResponse response = scheduleModuleService.importModule(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Lấy thông tin một module theo ID.
     * GET /schedule-modules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleModuleResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(scheduleModuleService.getById(id)));
    }

    /**
     * Lấy tất cả module của một buổi học.
     * GET /schedule-modules/by-schedule/{scheduleId}
     */
    @GetMapping("/by-schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<List<ScheduleModuleResponse>>> getBySchedule(
            @PathVariable String scheduleId) {

        return ResponseEntity.ok(ApiResponse.ok(scheduleModuleService.getByScheduleId(scheduleId)));
    }

    /**
     * Xoá mềm một module.
     * DELETE /schedule-modules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeModule(@PathVariable String id) {
        scheduleModuleService.removeModule(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Sắp xếp lại thứ tự module trong buổi học.
     * PUT /schedule-modules/by-schedule/{scheduleId}/reorder
     */
    @PutMapping("/by-schedule/{scheduleId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderModules(
            @PathVariable String scheduleId,
            @RequestBody @Valid ReorderScheduleModulesRequest request) {

        scheduleModuleService.reorderModules(scheduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
