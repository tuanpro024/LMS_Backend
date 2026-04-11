package com.lms.listening.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.listening.dto.request.AddVideoRequest;
import com.lms.listening.dto.response.VideoMetadataResponse;
import com.lms.listening.service.VideoMetadataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing video selection in listening practice study sets
 */
@RestController
@RequestMapping("/api/listening-practice/study-sets")
@RequiredArgsConstructor
@Slf4j
public class VideoSelectionController {

        private final VideoMetadataService videoMetadataService;

        /**
         * Add video to study set
         */
        @PostMapping("/{studySetId}/videos")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
        @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
        public ResponseEntity<ApiResponse<VideoMetadataResponse>> addVideoToStudySet(
                        @PathVariable String studySetId,
                        @RequestBody @Valid AddVideoRequest request,
                        Authentication authentication) {
                log.info("Adding video to study set: {}", studySetId);
                String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                                : null;
                VideoMetadataResponse response = videoMetadataService.addVideoToStudySet(studySetId, request, userId);
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
        }

        /**
         * Get all videos in study set
         */
        @GetMapping("/{studySetId}/videos")
        public ResponseEntity<ApiResponse<List<VideoMetadataResponse>>> getVideosInStudySet(
                        @PathVariable String studySetId) {
                log.info("Getting videos in study set: {}", studySetId);
                List<VideoMetadataResponse> response = videoMetadataService.getVideosInStudySet(studySetId);
                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        /**
         * Remove video from study set
         */
        @DeleteMapping("/{studySetId}/videos/{videoCode}")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
        @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
        public ResponseEntity<ApiResponse<Void>> removeVideoFromStudySet(
                        @PathVariable String studySetId,
                        @PathVariable String videoCode,
                        Authentication authentication) {
                log.info("Removing video {} from study set {}", videoCode, studySetId);
                String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                                : null;
                videoMetadataService.removeVideoFromStudySet(studySetId, videoCode, userId);
                return ResponseEntity.ok(ApiResponse.ok(null));
        }

        /**
         * Update video display order
         */
        @PutMapping("/{studySetId}/videos/{videoCode}/display-order")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
        @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
        public ResponseEntity<ApiResponse<VideoMetadataResponse>> updateVideoDisplayOrder(
                        @PathVariable String studySetId,
                        @PathVariable String videoCode,
                        @RequestParam Integer displayOrder,
                        Authentication authentication) {
                log.info("Updating display order for video {} in study set {}", videoCode, studySetId);
                String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                                : null;
                VideoMetadataResponse response = videoMetadataService.updateVideoDisplayOrder(studySetId, videoCode,
                                displayOrder, userId);
                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        /**
         * Refresh video metadata
         */
        @PostMapping("/{studySetId}/videos/{videoCode}/refresh")
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
        @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
        public ResponseEntity<ApiResponse<VideoMetadataResponse>> refreshVideoMetadata(
                        @PathVariable String studySetId,
                        @PathVariable String videoCode) {
                log.info("Refreshing metadata for video {} in study set {}", videoCode, studySetId);
                VideoMetadataResponse response = videoMetadataService.refreshVideoMetadata(studySetId, videoCode);
                return ResponseEntity.ok(ApiResponse.ok(response));
        }
}
