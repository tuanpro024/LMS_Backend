package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.videocourse.dto.request.CreateVideoStepRequest;
import com.lms.videocourse.dto.request.UpdateVideoStepRequest;
import com.lms.videocourse.dto.response.VideoStepResponse;
import com.lms.videocourse.service.IVideoStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Video Steps within a VideoCourse.
 */
@RestController
@RequestMapping("/video-steps")
@RequiredArgsConstructor
public class VideoStepController {

    private final IVideoStepService videoStepService;

    /** Create a new step (Admin/Teacher only) */
    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<VideoStepResponse>> createVideoStep(
            @RequestBody @Valid CreateVideoStepRequest request,
            Authentication authentication) {

        VideoStepResponse response = videoStepService.createVideoStep(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** Get step by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoStepResponse>> getVideoStepById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(videoStepService.getVideoStepById(id)));
    }

    /**
     * Get all steps for a course.
     * If authenticated, includes unlock status and progress per step.
     */
    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<List<VideoStepResponse>>> getVideoStepsByStudySet(
            @PathVariable String studySetId,
            Authentication authentication) {

        String userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = ((AuthPrincipal) authentication.getPrincipal()).userId();
        }
        List<VideoStepResponse> response = videoStepService.getVideoStepsByCourseId(studySetId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Update a step (Admin/Teacher only) */
    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<VideoStepResponse>> updateVideoStep(
            @PathVariable String id,
            @RequestBody @Valid UpdateVideoStepRequest request,
            Authentication authentication) {

        VideoStepResponse response = videoStepService.updateVideoStep(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Delete (soft-delete) a step (Admin/Teacher only) */
    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.VIDEO_COURSE)
    public ResponseEntity<ApiResponse<Void>> deleteVideoStep(
            @PathVariable String id,
            Authentication authentication) {

        videoStepService.deleteVideoStep(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
