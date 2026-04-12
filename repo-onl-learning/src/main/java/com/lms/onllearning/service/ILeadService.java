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
         * Admin/Manager kích hoạt thủ công quyền học cho người đăng ký.
         * Chuyển status từ PENDING_SALES → APPROVED, ghi nhận adminUserId và thời điểm.
         *
         * @param leadId      ID của lead registration cần kích hoạt
         * @param adminUserId userId của admin/manager thực hiện hành động (lấy từ JWT)
         */
        LeadRegistrationResponse activateLead(String leadId, String adminUserId);

        /**
         * Lấy thông tin đăng ký của user hiện tại cho một khóa học cụ thể.
         * Frontend dùng để kiểm tra trạng thái trước khi hiển thị timetable.
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
