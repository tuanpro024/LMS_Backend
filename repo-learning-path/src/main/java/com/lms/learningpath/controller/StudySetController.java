package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.response.ModuleResponse;
import com.lms.learningpath.dto.response.SetProgressResponse;
import com.lms.learningpath.service.LearningModuleService;
import com.lms.learningpath.service.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
@Slf4j
public class StudySetController {

    private final LearningModuleService moduleService;
    private final ProgressTrackingService progressTrackingService;

    /**
     * Get all modules of a study set (with user progress)
     */
    @GetMapping("/{studySetId}/modules")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getModulesByStudySet(
            @PathVariable String studySetId,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        List<ModuleResponse> response = moduleService.getModulesByStudySetId(studySetId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get user progress on a study set
     */
    @GetMapping("/{studySetId}/progress")
    public ResponseEntity<ApiResponse<SetProgressResponse>> getSetProgress(
            @PathVariable String studySetId,
            Authentication authentication
    ) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        SetProgressResponse response = progressTrackingService.getSetProgress(principal.userId(), studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}