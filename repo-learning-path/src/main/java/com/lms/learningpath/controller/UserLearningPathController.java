package com.lms.learningpath.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.FolderApiDelegate;
import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.response.FolderResponse;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.learningpath.dto.response.StudySetModuleResponse;
import com.lms.learningpath.dto.response.StudySetProgressDto;
import com.lms.learningpath.service.IProgressTrackingService;
import com.lms.learningpath.service.IUnlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User-facing controller for learning path interaction.
 * Provides endpoints for users to view their learning paths, progress, and
 * access content.
 */
@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class UserLearningPathController {

    private final PackageApiDelegate packageDelegate;
    private final FolderApiDelegate folderDelegate;
    private final IProgressTrackingService progressService;
    private final IUnlockService unlockService;

    /**
     * Get learning path (Package) with user progress
     */
    @GetMapping("/learning-paths/{packageId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLearningPath(
            @PathVariable String packageId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        // Get package info
        PackageResponse packageInfo = packageDelegate.getPackageById(packageId);

        // Get folders in this package
        List<FolderResponse> folders = folderDelegate.getFoldersByPackageId(packageId);

        // Build response with package and folders
        Map<String, Object> response = new HashMap<>();
        response.put("package", packageInfo);
        response.put("folders", folders);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get folder with StudySets and their unlock status
     */
    @GetMapping("/folders/{folderId}/study-sets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFolderStudySets(
            @PathVariable String folderId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        // Get folder info
        FolderResponse folderInfo = folderDelegate.getFolderById(folderId);

        // Get study sets with progress and unlock status
        // This will be enriched by the frontend or another service call
        Map<String, Object> response = new HashMap<>();
        response.put("folder", folderInfo);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get StudySet details with all modules and user progress
     */
    @GetMapping("/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudySetWithModules(
            @PathVariable String studySetId,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        String userId = principal.userId();

        // Get modules with progress
        List<StudySetModuleResponse> modules = progressService.getModulesWithProgress(userId, studySetId);

        // Get overall StudySet progress
        StudySetProgressDto progress = progressService.getStudySetProgress(userId, studySetId);

        // Check unlock status
        boolean isUnlocked = unlockService.isStudySetUnlocked(userId, studySetId);
        String lockReason = isUnlocked ? null : unlockService.getLockReason(userId, studySetId);

        Map<String, Object> response = new HashMap<>();
        response.put("studySetId", studySetId);
        response.put("modules", modules);
        response.put("progress", progress);
        response.put("isUnlocked", isUnlocked);
        response.put("lockReason", lockReason);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get user's overall progress across all learning paths
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverallProgress(
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();

        // This would aggregate progress across all packages/folders
        // For now, return basic info
        Map<String, Object> response = new HashMap<>();
        response.put("userId", principal.userId());
        response.put("message", "Overall progress endpoint - to be implemented");

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
