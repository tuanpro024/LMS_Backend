package com.lms.onllearning.service;

import com.lms.onllearning.entity.LeadRegistration;

/**
 * Service gửi email liên quan đến Lead Registration.
 * Tất cả các method đều bất đồng bộ (@Async) để không block luồng chính.
 */
public interface ILeadEmailService {

    /**
     * Gửi email xác nhận đã nhận phiếu đăng ký (khi status → PENDING_SALES).
     * Nội dung: "Chúng tôi đã nhận đăng ký của bạn và đang chờ bộ phận tư vấn xác nhận."
     */
    void sendRegistrationConfirmationEmail(LeadRegistration lead);

    /**
     * Gửi email thông báo quyền học đã được kích hoạt (khi status → APPROVED).
     * Nội dung: thông báo học viên có thể truy cập thời khóa biểu.
     */
    void sendActivationEmail(LeadRegistration lead);
}
