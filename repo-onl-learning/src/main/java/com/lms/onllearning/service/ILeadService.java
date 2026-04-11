package com.lms.onllearning.service;

import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ILeadService {
    /** Tạo mới lead hoặc trả về lead cũ (idempotent) */
    LeadRegistrationResponse register(LeadRegistrationRequest request, String userId);

    /** Danh sách leads cho Admin/Staff với filter */
    Page<LeadRegistrationResponse> getLeads(
            String courseCode,
            LocalDate from,
            LocalDate to,
            Pageable pageable);
}
