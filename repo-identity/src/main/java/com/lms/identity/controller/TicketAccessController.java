package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.identity.entity.ticket.TicketModule;
import com.lms.identity.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API — chỉ dành cho các service nội bộ gọi để kiểm tra quyền ticket.
 * Cần bảo vệ bằng internal secret header trong API Gateway hoặc network policy.
 */
@RestController
@RequestMapping("/internal/tickets")
@RequiredArgsConstructor
public class TicketAccessController {

    private final TicketService ticketService;

    /**
     * Kiểm tra xem userId có ticket hợp lệ (OPEN hoặc DONE) cho module không.
     *
     * @param userId ID của user cần kiểm tra
     * @param module Tên module (khớp với TicketModule enum)
     * @return true nếu có quyền CUD, false nếu không
     */
    @GetMapping("/check-access")
    public ApiResponse<Boolean> checkAccess(
            @RequestParam("userId") String userId,
            @RequestParam("module") String module) {
        TicketModule ticketModule;
        try {
            ticketModule = TicketModule.valueOf(module.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.ok(false);
        }
        boolean hasAccess = ticketService.checkAccess(userId, ticketModule);
        return ApiResponse.ok(hasAccess);
    }

    /**
     * Kiểm tra xem userId có bất kỳ ticket hợp lệ nào (bất kỳ module) không.
     * Dùng bởi các content service để xác định ticket-holder được xem DRAFT của mình.
     *
     * @param userId ID của user cần kiểm tra
     * @return true nếu user có ít nhất 1 ticket OPEN/DONE
     */
    @GetMapping("/has-any")
    public ApiResponse<Boolean> hasAnyTicket(@RequestParam("userId") String userId) {
        boolean result = ticketService.hasAnyTicket(userId);
        return ApiResponse.ok(result);
    }
}
