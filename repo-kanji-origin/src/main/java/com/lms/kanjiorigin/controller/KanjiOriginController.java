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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanji-origins")
@RequiredArgsConstructor
public class KanjiOriginController {

    private final KanjiOriginService kanjiOriginService;
    private final KanjiProgressService kanjiProgressService;

    @PostMapping
    @RequiresTicket(module = TicketModuleEnum.KANJI_ORIGIN)
    public ApiResponse<KanjiOriginResponse> createOrigin(
            @Valid @RequestBody CreateKanjiOriginRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(kanjiOriginService.createOrigin(request, principal.userId()));
    }

    @PutMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.KANJI_ORIGIN)
    public ApiResponse<KanjiOriginResponse> updateOrigin(
            @PathVariable String id,
            @Valid @RequestBody UpdateKanjiOriginRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(kanjiOriginService.updateOrigin(id, request, principal.userId()));
    }

    @DeleteMapping("/{id}")
    @RequiresTicket(module = TicketModuleEnum.KANJI_ORIGIN)
    public ApiResponse<Void> deleteOrigin(
            @PathVariable String id,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        kanjiOriginService.deleteOrigin(id, principal.userId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<KanjiOriginResponse> getOrigin(@PathVariable String id) {
        return ApiResponse.ok(kanjiOriginService.getOrigin(id));
    }

    @GetMapping
    public ApiResponse<List<KanjiOriginResponse>> search(
            @Valid KanjiOriginSearchRequest request,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        return ApiResponse.ok(kanjiOriginService.search(request, userId));
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<KanjiOriginResponse>> searchPaged(
            @Valid KanjiOriginSearchRequest request,
            Authentication authentication) {
        String userId = extractUserId(authentication);
        return ApiResponse.ok(kanjiOriginService.searchPaged(request, userId));
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return null;
        }
        return principal.userId();
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
