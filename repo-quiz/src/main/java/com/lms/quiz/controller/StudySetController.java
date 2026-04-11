package com.lms.quiz.controller;

import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final StudySetApiDelegate delegate;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<StudySetResponse>> createStudySet(
            @RequestBody @Valid CreateStudySetRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = delegate.createStudySet(request, principal.userId());
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
            @RequestParam(required = false) String packageType) {
        if (userId != null) {
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
        List<StudySetResponse> response = delegate.getAllStudySets();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/exact-match")
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> findByTitleAndUserIdIgnoreCase(
            @RequestParam String title,
            @RequestParam String userId) {
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
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<StudySetResponse>> updateStudySet(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = delegate.updateStudySet(id, request, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.QUIZ)
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        delegate.deleteStudySet(id, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
