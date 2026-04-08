package com.lms.pronunciation.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.pronunciation.dto.request.CreatePronunciationItemRequest;
import com.lms.pronunciation.dto.request.PronunciationItemSearchRequest;
import com.lms.pronunciation.dto.request.UpdatePronunciationItemRequest;
import com.lms.pronunciation.dto.response.PronunciationItemResponse;
import com.lms.pronunciation.service.PronunciationItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pronunciation-items")
@RequiredArgsConstructor
public class PronunciationItemController {

    private final PronunciationItemService pronunciationItemService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<PronunciationItemResponse> create(@Valid @RequestBody CreatePronunciationItemRequest request) {
        return ApiResponse.ok(pronunciationItemService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<PronunciationItemResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdatePronunciationItemRequest request) {
        return ApiResponse.ok(pronunciationItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        pronunciationItemService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<PronunciationItemResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(pronunciationItemService.getById(id));
    }

    @GetMapping("/study-set/{studySetId}")
    public ApiResponse<List<PronunciationItemResponse>> getByStudySetId(@PathVariable String studySetId) {
        return ApiResponse.ok(pronunciationItemService.getByStudySetId(studySetId));
    }

    @GetMapping
    public ApiResponse<List<PronunciationItemResponse>> search(PronunciationItemSearchRequest request) {
        return ApiResponse.ok(pronunciationItemService.search(request));
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<PronunciationItemResponse>> searchPaged(PronunciationItemSearchRequest request) {
        return ApiResponse.ok(pronunciationItemService.searchPaged(request));
    }
}
