package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing StudySets in the Video Course context.
 * A StudySet holds one or more VideoCourses (e.g. "HSK 1 - Video Bài 1").
 * Delegates to StudySetApiDelegate from repo-content-common.
 */
@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final StudySetApiDelegate studySetDelegate;

    @PostMapping
    public ResponseEntity<ApiResponse<StudySetResponse>> createStudySet(
            @RequestBody @Valid CreateStudySetRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = studySetDelegate.createStudySet(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> getStudySetById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(studySetDelegate.getStudySetById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getAllStudySets(
            @RequestParam(required = false) String q) {
        List<StudySetResponse> response = (q != null && !q.isBlank())
                ? studySetDelegate.searchStudySets(q)
                : studySetDelegate.getAllStudySets();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getStudySetsByFolder(
            @PathVariable String folderId) {
        return ResponseEntity.ok(ApiResponse.ok(studySetDelegate.getStudySetsByFolderId(folderId)));
    }

    @GetMapping("/my-sets")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getMyStudySets(
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(studySetDelegate.getStudySetsByUserId(principal.userId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> updateStudySet(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                studySetDelegate.updateStudySet(id, request, principal.userId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        studySetDelegate.deleteStudySet(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
