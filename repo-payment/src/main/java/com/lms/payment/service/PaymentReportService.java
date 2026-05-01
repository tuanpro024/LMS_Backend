package com.lms.payment.service;

import com.lms.payment.dto.response.LmsOverviewStatsResponse;
import com.lms.payment.dto.response.PaymentSummaryResponse;
import com.lms.payment.dto.response.TransactionReportItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentReportService {
    PaymentSummaryResponse getSummary();
    LmsOverviewStatsResponse getLmsOverviewStats();
    Page<TransactionReportItemResponse> getTransactionReport(Pageable pageable);
}
