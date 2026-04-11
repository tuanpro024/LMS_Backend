package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.support.SupportTicketCommentRequest;
import com.lms.identity.dto.response.support.SupportTicketCommentResponse;
import com.lms.identity.dto.response.support.SupportTicketResponse;
import com.lms.identity.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
public class SupportTicketManagementController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ApiResponse<PageResponse<SupportTicketResponse>> list(Authentication authentication,
                                                                 @RequestParam(required = false) String search,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        Page<SupportTicketResponse> result = supportTicketService.listManagementTickets(principal, search, status, page, size);
        return ApiResponse.ok(PageResponse.<SupportTicketResponse>builder()
                .items(result.getContent())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .build());
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<SupportTicketCommentResponse> reply(Authentication authentication,
                                                           @PathVariable String id,
                                                           @Valid @RequestBody SupportTicketCommentRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(supportTicketService.replyTicket(principal, id, request));
    }
}
