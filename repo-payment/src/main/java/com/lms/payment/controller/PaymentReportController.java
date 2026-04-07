package com.lms.payment.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.payment.dto.response.PaymentSummaryResponse;
import com.lms.payment.dto.response.TransactionReportItemResponse;
import com.lms.payment.service.PaymentReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class PaymentReportController {

    private final PaymentReportService paymentReportService;

    public PaymentReportController(PaymentReportService paymentReportService) {
        this.paymentReportService = paymentReportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ApiResponse<PaymentSummaryResponse> getSummary() {
        return ApiResponse.ok(paymentReportService.getSummary());
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ApiResponse<Page<TransactionReportItemResponse>> getTransactionReport(Pageable pageable) {
        return ApiResponse.ok(paymentReportService.getTransactionReport(pageable));
    }
}
