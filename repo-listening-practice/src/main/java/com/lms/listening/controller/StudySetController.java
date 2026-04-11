package com.lms.listening.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
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

/**
 * Thin controller that delegates to StudySetApiDelegate from content-common
 */
@RestController
@RequestMapping("/api/listening-practice/study-sets")
@RequiredArgsConstructor
public class StudySetController {

    private final StudySetApiDelegate delegate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<StudySetResponse>> createStudySet(
            @RequestBody @Valid CreateStudySetRequest request,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        StudySetResponse response = delegate.createStudySet(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> getStudySetById(@PathVariable String id) {
        StudySetResponse response = delegate.getStudySetById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getStudySets(
            @RequestParam(required = false) String folderId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String packageType) {
        List<StudySetResponse> response;
        if (folderId != null) {
            response = delegate.getStudySetsByFolderId(folderId);
        } else if (userId != null) {
            response = delegate.getStudySetsByUserId(userId);
        } else if (packageType != null) {
            response = delegate.getStudySetsByPackageType(packageType);
        } else {
            response = delegate.getAllStudySets();
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<StudySetResponse>> updateStudySet(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        StudySetResponse response = delegate.updateStudySet(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.LISTENING_PRACTICE)
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(
            @PathVariable String id,
            Authentication authentication) {
        String userId = (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal)
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;
        delegate.deleteStudySet(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
