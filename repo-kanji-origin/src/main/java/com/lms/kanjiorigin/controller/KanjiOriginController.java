package com.lms.kanjiorigin.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.KanjiOriginSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.service.KanjiOriginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-origins")
@RequiredArgsConstructor
public class KanjiOriginController {

    private final KanjiOriginService kanjiOriginService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<KanjiOriginResponse> createOrigin(@Valid @RequestBody CreateKanjiOriginRequest request) {
        return ApiResponse.ok(kanjiOriginService.createOrigin(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<KanjiOriginResponse> updateOrigin(
            @PathVariable String id,
            @Valid @RequestBody UpdateKanjiOriginRequest request) {
        return ApiResponse.ok(kanjiOriginService.updateOrigin(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ApiResponse<Void> deleteOrigin(@PathVariable String id) {
        kanjiOriginService.deleteOrigin(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<KanjiOriginResponse> getOrigin(@PathVariable String id) {
        return ApiResponse.ok(kanjiOriginService.getOrigin(id));
    }

    @GetMapping
    public ApiResponse<List<KanjiOriginResponse>> search(KanjiOriginSearchRequest request) {
        return ApiResponse.ok(kanjiOriginService.search(request));
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<KanjiOriginResponse>> searchPaged(KanjiOriginSearchRequest request) {
        return ApiResponse.ok(kanjiOriginService.searchPaged(request));
    }
}
