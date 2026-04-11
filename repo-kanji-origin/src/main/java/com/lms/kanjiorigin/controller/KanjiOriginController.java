package com.lms.kanjiorigin.controller;

import com.lms.common.security.RequiresTicket;
import com.lms.common.security.TicketModuleEnum;
import com.lms.common.dto.ApiResponse;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.dto.PageResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.kanjiorigin.dto.request.CreateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.request.KanjiOriginSearchRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiStatusRequest;
import com.lms.kanjiorigin.dto.request.UpdateKanjiOriginRequest;
import com.lms.kanjiorigin.dto.response.KanjiOriginResponse;
import com.lms.kanjiorigin.dto.response.KanjiStatusResponse;
import com.lms.kanjiorigin.service.KanjiOriginService;
import com.lms.kanjiorigin.service.KanjiProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-origins")
@RequiredArgsConstructor
public class KanjiOriginController {

    private final KanjiOriginService kanjiOriginService;
    private final KanjiProgressService kanjiProgressService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.KANJI_ORIGIN)
    public ApiResponse<KanjiOriginResponse> createOrigin(@Valid @RequestBody CreateKanjiOriginRequest request) {
        return ApiResponse.ok(kanjiOriginService.createOrigin(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.KANJI_ORIGIN)
    public ApiResponse<KanjiOriginResponse> updateOrigin(
            @PathVariable String id,
            @Valid @RequestBody UpdateKanjiOriginRequest request) {
        return ApiResponse.ok(kanjiOriginService.updateOrigin(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER', 'TEACHER', 'COLLABORATOR')")
    @RequiresTicket(module = TicketModuleEnum.KANJI_ORIGIN)
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

    @PatchMapping("/{id}/status")
    public ApiResponse<KanjiStatusResponse> updateKanjiStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateKanjiStatusRequest request,
            Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        return ApiResponse.ok(kanjiProgressService.updateKanjiStatus(principal.userId(), id, request));
    }
}
