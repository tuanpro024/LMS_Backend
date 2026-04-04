package com.lms.aipractice.controller;

import com.lms.aipractice.dto.request.CreateAiPracticeItemRequest;
import com.lms.aipractice.dto.request.UpdateAiPracticeItemRequest;
import com.lms.aipractice.dto.response.AiPracticeItemResponse;
import com.lms.aipractice.service.AiPracticeItemService;
import com.lms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai-items")
@RequiredArgsConstructor
public class AiPracticeItemController {

    private final AiPracticeItemService itemService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'TEACHER_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AiPracticeItemResponse>> create(
            @Valid @RequestBody CreateAiPracticeItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(itemService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TEACHER_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AiPracticeItemResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAiPracticeItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TEACHER_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiPracticeItemResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getById(id)));
    }

    @GetMapping("/study-set/{studySetId}")
    public ResponseEntity<ApiResponse<List<AiPracticeItemResponse>>> getByStudySet(
            @PathVariable String studySetId) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getByStudySetId(studySetId)));
    }
}
