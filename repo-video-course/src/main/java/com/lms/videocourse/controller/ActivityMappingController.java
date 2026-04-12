package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.dto.request.ActivityMappingRequest;
import com.lms.videocourse.dto.response.ActivityMappingDTO;
import com.lms.videocourse.dto.response.GenerationStatusDTO;
import com.lms.videocourse.service.ActivityMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for managing activity-module mappings.
 * Used by Admin to map syllabus activities to internal practice modules
 * before generating actual courses.
 */
@RestController
@RequestMapping("/syllabus/activity-mappings")
@RequiredArgsConstructor
public class ActivityMappingController {

    private final ActivityMappingService mappingService;

    /**
     * Get mapping for a specific activity.
     */
    @GetMapping("/{activityId}")
    public ResponseEntity<ApiResponse<ActivityMappingDTO>> getMapping(
            @PathVariable String activityId) {
        ActivityMappingDTO dto = mappingService.getMappingByActivityId(activityId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    /**
     * Get mappings for multiple activities (batch).
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, ActivityMappingDTO>>> getMappingsBatch(
            @RequestBody List<String> activityIds) {
        Map<String, ActivityMappingDTO> data = mappingService.getMappingsByActivityIds(activityIds);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Create or update mapping for an activity.
     */
    @PutMapping("/{activityId}")
    public ResponseEntity<ApiResponse<ActivityMappingDTO>> saveMapping(
            @PathVariable String activityId,
            @RequestBody ActivityMappingRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ActivityMappingDTO dto = mappingService.saveMapping(activityId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    /**
     * Delete mapping for an activity.
     */
    @DeleteMapping("/{activityId}")
    public ResponseEntity<ApiResponse<Void>> deleteMapping(
            @PathVariable String activityId) {
        mappingService.deleteMapping(activityId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Get generation readiness status for a course.
     */
    @GetMapping("/generation-status/{cmsCourseId}")
    public ResponseEntity<ApiResponse<GenerationStatusDTO>> getGenerationStatus(
            @PathVariable String cmsCourseId) {
        GenerationStatusDTO status = mappingService.getGenerationStatus(cmsCourseId);
        return ResponseEntity.ok(ApiResponse.ok(status));
    }
}
