package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.request.OnlineCourseRequest;
import com.lms.onllearning.dto.response.OnlineCourseFullDetailResponse;
import com.lms.onllearning.dto.response.OnlineCourseResponse;
import com.lms.onllearning.service.IOnlineCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class OnlineCourseController {

    private final IOnlineCourseService courseService;

    // -----------------------------------------------------------------------
    // Public endpoints (hiển thị catalogue khóa học)
    // -----------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<List<OnlineCourseResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OnlineCourseResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getById(id)));
    }

    @GetMapping("{id}/full-detail")
    public ResponseEntity<ApiResponse<OnlineCourseFullDetailResponse>> getFullDetail(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getFullDetail(id)));
    }

    // -----------------------------------------------------------------------
    // Admin endpoints
    // -----------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<OnlineCourseResponse>> create(
            @Valid @RequestBody OnlineCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<OnlineCourseResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody OnlineCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        courseService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
