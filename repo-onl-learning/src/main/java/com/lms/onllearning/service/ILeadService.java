package com.lms.onllearning.service;

import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ILeadService {

        /**
         * Tạo mới lead hoặc trả về lead cũ (idempotent).
         * Nếu lead cũ có status = COMPLETED hoặc CANCELED → reset về PENDING (cho phép đăng ký lại).
         * Nếu đang PENDING hoặc IN_PROGRESS → trả về lead cũ (không tạo duplicate).
         * Sau khi tạo mới → status = PENDING + gửi notification.
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

        /**
         * Đánh dấu lead đã hoàn thành khóa học → status = COMPLETED.
         * Cho phép học viên đăng ký lại khóa học này.
         *
         * @param userId     userId từ JWT
         * @param courseCode mã khóa học
         */
        void markCourseCompleted(String userId, String courseCode);
}
