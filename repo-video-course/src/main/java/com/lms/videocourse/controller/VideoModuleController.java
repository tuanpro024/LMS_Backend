package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.videocourse.dto.request.CreateVideoModuleRequest;
import com.lms.videocourse.dto.request.UpdateVideoModuleRequest;
import com.lms.videocourse.dto.response.VideoModuleResponse;
import com.lms.videocourse.service.IVideoModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Video Modules (individual video lessons) within
 * a VideoStep.
 * VideoModule stores video data inline (videoUrl, thumbnailUrl, duration,
 * subtitles).
 */
@RestController
@RequestMapping("/video-modules")
@RequiredArgsConstructor
public class VideoModuleController {

    private final IVideoModuleService videoModuleService;

    /** Create a new video module (Admin/Teacher only) */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<VideoModuleResponse>> createVideoModule(
            @RequestBody @Valid CreateVideoModuleRequest request,
            Authentication authentication) {

        VideoModuleResponse response = videoModuleService.createVideoModule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Get module by ID.
     * If authenticated, includes watch progress.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoModuleResponse>> getVideoModuleById(
            @PathVariable String id,
            Authentication authentication) {

        String userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = ((AuthPrincipal) authentication.getPrincipal()).userId();
        }
        return ResponseEntity.ok(ApiResponse.ok(videoModuleService.getVideoModuleById(id, userId)));
    }

    /**
     * Get all modules for a step.
     * If authenticated, includes watch progress per module.
     */
    @GetMapping("/step/{stepId}")
    public ResponseEntity<ApiResponse<List<VideoModuleResponse>>> getVideoModulesByStep(
            @PathVariable String stepId,
            Authentication authentication) {

        String userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = ((AuthPrincipal) authentication.getPrincipal()).userId();
        }
        return ResponseEntity.ok(ApiResponse.ok(videoModuleService.getVideoModulesByStepId(stepId, userId)));
    }

    /** Update a video module (Admin/Teacher only) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<VideoModuleResponse>> updateVideoModule(
            @PathVariable String id,
            @RequestBody UpdateVideoModuleRequest request,
            Authentication authentication) {

        VideoModuleResponse response = videoModuleService.updateVideoModule(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Delete (soft-delete) a video module (Admin/Teacher only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<Void>> deleteVideoModule(
            @PathVariable String id,
            Authentication authentication) {

        videoModuleService.deleteVideoModule(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
