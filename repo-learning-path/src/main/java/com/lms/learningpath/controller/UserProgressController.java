package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.dto.response.StudySetProgressDto;
import com.lms.learningpath.service.IProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final IProgressTrackingService progressService;

    @PostMapping("/module/{moduleId}/start")
    public ResponseEntity<ApiResponse<ModuleProgressDto>> startModule(
            @PathVariable String moduleId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleProgressDto response = progressService.startModule(principal.userId(), moduleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/module/{moduleId}/update")
    public ResponseEntity<ApiResponse<ModuleProgressDto>> updateProgress(
            @PathVariable String moduleId,
            @RequestBody @Valid UpdateProgressRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleProgressDto response = progressService.updateProgress(
                principal.userId(), moduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/module/{moduleId}/complete")
    public ResponseEntity<ApiResponse<ModuleProgressDto>> completeModule(
            @PathVariable String moduleId,
            @RequestBody @Valid CompleteModuleRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ModuleProgressDto response = progressService.completeModule(
                principal.userId(), moduleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<StudySetProgressDto>> getStudySetProgress(
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetProgressDto response = progressService.getStudySetProgress(
                principal.userId(), studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
