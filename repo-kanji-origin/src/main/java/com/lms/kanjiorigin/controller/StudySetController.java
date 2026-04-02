package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.security.AuthPrincipal;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.kanjiorigin.dto.response.KanjiStatusResponse;
import com.lms.kanjiorigin.dto.response.KanjiStudySetProgressResponse;
import com.lms.kanjiorigin.service.KanjiProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final StudySetApiDelegate delegate;
    private final KanjiProgressService kanjiProgressService;

    private AuthPrincipal extractPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }

    private String extractUserId(Authentication authentication) {
        return extractPrincipal(authentication).userId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudySetResponse>> createStudySet(
            @RequestBody @Valid CreateStudySetRequest request,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        StudySetResponse response = delegate.createStudySet(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> getStudySetById(@PathVariable String id) {
        StudySetResponse response = delegate.getStudySetById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getAllStudySets(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String packageType,
            Authentication authentication) {
        AuthPrincipal principal = extractPrincipal(authentication);
        String currentUserId = principal.userId();
        boolean isAdmin = principal.hasRole("ROLE_ADMIN");

        if (userId != null && !userId.isBlank()) {
            if (!isAdmin && !currentUserId.equals(userId)) {
                throw new ApiException(ErrorCode.FORBIDDEN, "No permission to access other user's study sets");
            }
            List<StudySetResponse> response = delegate.getStudySetsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
        if (q != null) {
            List<StudySetResponse> response = delegate.searchStudySets(q);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }
        if (packageType != null) {
            List<StudySetResponse> response = delegate.getStudySetsByPackageType(packageType);
            return ResponseEntity.ok(ApiResponse.ok(response));
        }

        List<StudySetResponse> response = isAdmin
                ? delegate.getAllStudySets()
                : delegate.getStudySetsByUserId(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/exact-match")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> findByTitleAndUserIdIgnoreCase(
            @RequestParam String title,
            @RequestParam String userId,
            Authentication authentication) {
        AuthPrincipal principal = extractPrincipal(authentication);
        boolean isAdmin = principal.hasRole("ROLE_ADMIN");
        if (!isAdmin && !principal.userId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "No permission to access other user's study sets");
        }

        List<StudySetResponse> response = delegate.findByTitleAndUserIdIgnoreCase(title, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getStudySetsByFolderId(
            @PathVariable String folderId) {
        List<StudySetResponse> response = delegate.getStudySetsByFolderId(folderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> updateStudySet(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        StudySetResponse response = delegate.updateStudySet(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(
            @PathVariable String id,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        delegate.deleteStudySet(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{studySetId}/progress")
    public ResponseEntity<ApiResponse<KanjiStudySetProgressResponse>> getStudySetProgress(
            @PathVariable String studySetId,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        KanjiStudySetProgressResponse response = kanjiProgressService.getStudySetProgress(userId,
                studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{studySetId}/kanji-statuses")
    public ResponseEntity<ApiResponse<List<KanjiStatusResponse>>> getStudySetKanjiStatuses(
            @PathVariable String studySetId,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        List<KanjiStatusResponse> response = kanjiProgressService.getStudySetKanjiStatuses(userId,
                studySetId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
