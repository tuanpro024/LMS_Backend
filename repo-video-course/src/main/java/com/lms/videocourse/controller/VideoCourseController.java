package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.videocourse.dto.request.CreateVideoCourseRequest;
import com.lms.videocourse.dto.request.UpdateVideoCourseRequest;
import com.lms.videocourse.dto.response.VideoCourseResponse;
import com.lms.videocourse.service.IVideoCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Video Courses.
 * Admin/Teacher can create, update, delete courses.
 * All authenticated users can view.
 */
@RestController
@RequestMapping("/video-courses")
@RequiredArgsConstructor
public class VideoCourseController {

    private final IVideoCourseService videoCourseService;

    /** Create a new video course (Admin/Teacher only) */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<VideoCourseResponse>> createVideoCourse(
            @RequestBody @Valid CreateVideoCourseRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        VideoCourseResponse response = videoCourseService.createVideoCourse(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** Get video course by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoCourseResponse>> getVideoCourseById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(videoCourseService.getVideoCourseById(id)));
    }

    /** Get all video courses for a study set */
    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<List<VideoCourseResponse>>> getVideoCoursesByStudySet(
            @PathVariable String studySetId) {
        return ResponseEntity.ok(ApiResponse.ok(videoCourseService.getVideoCoursesByStudySetId(studySetId)));
    }

    /** Update a video course (Admin/Teacher only) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<VideoCourseResponse>> updateVideoCourse(
            @PathVariable String id,
            @RequestBody @Valid UpdateVideoCourseRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        VideoCourseResponse response = videoCourseService.updateVideoCourse(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Delete (soft-delete) a video course (Admin/Teacher only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteVideoCourse(
            @PathVariable String id,
            Authentication authentication) {

        videoCourseService.deleteVideoCourse(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
