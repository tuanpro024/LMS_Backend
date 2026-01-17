package com.lms.writing.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.writing.dto.request.CreateStudySetRequest;
import com.lms.writing.dto.request.UpdateStudySetRequest;
import com.lms.writing.dto.response.StudySetResponse;
import com.lms.writing.service.StudySetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study-sets")
@RequiredArgsConstructor
@Slf4j
public class StudySetController {

    private final StudySetService studySetService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudySetResponse>> createStudySet(
            @RequestBody @Valid CreateStudySetRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = studySetService.createStudySet(request, principal.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> getStudySetById(
            @PathVariable String id,
            Authentication authentication) {

        String userId = authentication != null && authentication.getPrincipal() instanceof AuthPrincipal
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;

        StudySetResponse response = studySetService.getStudySetById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySetResponse>>> getStudySets(
            @RequestParam(required = false) String userId,
            Authentication authentication) {

        String currentUserId = authentication != null && authentication.getPrincipal() instanceof AuthPrincipal
                ? ((AuthPrincipal) authentication.getPrincipal()).userId()
                : null;

        List<StudySetResponse> response;

        if (userId != null) {
            response = studySetService.getStudySetsByUserId(userId, currentUserId);
        } else {
            response = studySetService.getAllPublicStudySets();
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySetResponse>> updateStudySet(
            @PathVariable String id,
            @RequestBody @Valid UpdateStudySetRequest request,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        StudySetResponse response = studySetService.updateStudySet(id, request, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(
            @PathVariable String id,
            Authentication authentication) {

        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        studySetService.deleteStudySet(id, principal.userId());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
