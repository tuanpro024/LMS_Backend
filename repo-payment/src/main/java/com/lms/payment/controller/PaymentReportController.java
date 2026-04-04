package com.lms.payment.controller;

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
@RequestMapping("/payment/reports")
public class PaymentReportController {

    private final PaymentReportService paymentReportService;

    public PaymentReportController(PaymentReportService paymentReportService) {
        this.paymentReportService = paymentReportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public PaymentSummaryResponse getSummary() {
        return paymentReportService.getSummary();
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public Page<TransactionReportItemResponse> getTransactionReport(Pageable pageable) {
        return paymentReportService.getTransactionReport(pageable);
    }
}
