package com.lms.onllearning.service;

import com.lms.onllearning.entity.LeadRegistration;

/**
 * Service gửi email liên quan đến Lead Registration.
 * Tất cả các method đều bất đồng bộ (@Async) để không block luồng chính.
 */
public interface ILeadEmailService {

    /**
     * Gửi email xác nhận đã nhận phiếu đăng ký (khi tạo mới hoặc đăng ký lại).
     * Nội dung: xác nhận đã tiếp nhận phiếu đăng ký.
     */
    void sendRegistrationConfirmationEmail(LeadRegistration lead);
}
