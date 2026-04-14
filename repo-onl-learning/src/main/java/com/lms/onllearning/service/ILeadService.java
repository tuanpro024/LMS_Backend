package com.lms.onllearning.service;

import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ILeadService {

        /**
         * Tạo mới lead hoặc trả về lead cũ (idempotent). Sau khi tạo mới → status =
         * PENDING_SALES + gửi notification
         */
        LeadRegistrationResponse register(LeadRegistrationRequest request, String userId);

        /** Danh sách leads cho Admin/Staff với filter */
        Page<LeadRegistrationResponse> getLeads(
                        String courseCode,
                        LocalDate from,
                        LocalDate to,
                        Pageable pageable);

        /**
         * Lấy thông tin đăng ký của user hiện tại cho một khóa học cụ thể.
         * Frontend dùng để hiển thị trạng thái đăng ký và thông tin liên quan.
         *
         * @param userId     userId từ JWT
         * @param courseCode mã khóa học
         */
        LeadRegistrationResponse getMyRegistration(String userId, String courseCode);

        /** Danh sách leads PENDING_SALES (không phân trang) cho tích hợp CMS */
        List<LeadRegistrationResponse> getAllLeads(
                        String courseCode,
                        LocalDate from,
                        LocalDate to);
}
