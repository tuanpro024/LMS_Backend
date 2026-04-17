package com.lms.content.common.delegate.api;

import com.lms.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/study-sets")
@RequiredArgsConstructor
public class InternalStudySetGuardController {

    private final StudySetApiDelegate studySetApiDelegate;

    @GetMapping("/{studySetId}/learning-allowed")
    public ResponseEntity<ApiResponse<Boolean>> isLearningAllowed(@PathVariable String studySetId) {
        boolean allowed = studySetApiDelegate.isStudySetLearningAllowed(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(allowed));
    }

    @GetMapping("/{studySetId}/learning-allowed/assert")
    public ResponseEntity<ApiResponse<Void>> assertLearningAllowed(@PathVariable String studySetId) {
        studySetApiDelegate.assertStudySetLearningAllowed(studySetId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
