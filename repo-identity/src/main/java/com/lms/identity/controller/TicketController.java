package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.dto.PageResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.identity.dto.request.ticket.CreateTicketRequest;
import com.lms.identity.dto.request.ticket.UpdateTicketRequest;
import com.lms.identity.dto.response.ticket.TicketResponse;
import com.lms.identity.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Tạo ticket mới — ADMIN / TEACHER_MANAGER
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER_MANAGER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TicketResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateTicketRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(ticketService.createTicket(principal, request));
    }

    /**
     * Lấy chi tiết ticket
     */
    @GetMapping("/{id}")
    public ApiResponse<TicketResponse> get(@PathVariable("id") String id) {
        return ApiResponse.ok(ticketService.getTicket(id));
    }

    /**
     * Danh sách ticket (phân trang).
     * ADMIN/MANAGER xem tất cả; TEACHER/COLLABORATOR chỉ xem của mình.
     */
    @GetMapping
    public ApiResponse<PageResponse<TicketResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        Page<TicketResponse> result = ticketService.listTickets(principal, page, size);
        PageResponse<TicketResponse> pageResponse = PageResponse.<TicketResponse>builder()
                .items(result.getContent())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .build();
        return ApiResponse.ok(pageResponse);
    }

    /**
     * Cập nhật ticket — ADMIN / TEACHER_MANAGER
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER_MANAGER', 'ADMIN')")
    public ApiResponse<TicketResponse> update(
            Authentication authentication,
            @PathVariable("id") String id,
            @RequestBody UpdateTicketRequest request) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(ticketService.updateTicket(id, principal, request));
    }

    /**
     * Xóa ticket — ADMIN / TEACHER_MANAGER
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER_MANAGER', 'ADMIN')")
    public ApiResponse<Void> delete(
            Authentication authentication,
            @PathVariable("id") String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        ticketService.deleteTicket(id, principal);
        return ApiResponse.ok(null);
    }

    /**
     * Người được assign đánh dấu DONE
     */
    @PostMapping("/{id}/done")
    public ApiResponse<TicketResponse> markDone(
            Authentication authentication,
            @PathVariable("id") String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(ticketService.markDone(id, principal));
    }

    /**
     * ADMIN / TEACHER_MANAGER CLOSE ticket
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('TEACHER_MANAGER', 'ADMIN')")
    public ApiResponse<TicketResponse> close(
            Authentication authentication,
            @PathVariable("id") String id) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(ticketService.closeTicket(id, principal));
    }
}
