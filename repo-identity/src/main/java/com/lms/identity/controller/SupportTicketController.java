package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.support.SupportTicketCreateRequest;
import com.lms.identity.dto.response.support.SupportTicketCommentResponse;
import com.lms.identity.dto.response.support.SupportTicketResponse;
import com.lms.identity.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ApiResponse<SupportTicketResponse> create(Authentication authentication,
                                                   @Valid @RequestBody SupportTicketCreateRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(supportTicketService.createTicket(principal, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<SupportTicketResponse>> list(Authentication authentication,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String category,
                                                                 @RequestParam(required = false) String priority,
                                                                 @RequestParam(required = false) String sortBy,
                                                                 @RequestParam(required = false) String sortDirection,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        Page<SupportTicketResponse> result = supportTicketService.listMyTickets(principal, status, category, priority, sortBy, sortDirection, page, size);
        return ApiResponse.ok(PageResponse.<SupportTicketResponse>builder()
                .items(result.getContent())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .build());
    }

    @GetMapping("/{id}")
    public ApiResponse<SupportTicketResponse> get(Authentication authentication,
                                                  @PathVariable String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(supportTicketService.getTicket(principal, id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SupportTicketResponse> cancel(Authentication authentication,
                                                     @PathVariable String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(supportTicketService.cancelTicket(principal, id));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<SupportTicketCommentResponse>> listComments(Authentication authentication,
                                                                        @PathVariable String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(supportTicketService.listComments(principal, id));
    }
}
