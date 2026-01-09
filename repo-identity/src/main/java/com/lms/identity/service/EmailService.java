package com.lms.identity.service;

public interface EmailService {
    /**
     * Gửi email xác thực tài khoản
     * 
     * @param toEmail          Email người nhận
     * @param userName         Tên người dùng
     * @param verificationLink Link xác thực
     */
    void sendVerificationEmail(String toEmail, String userName, String verificationLink);

    /**
     * Gửi email reset mật khẩu (để mở rộng sau)
     * 
     * @param toEmail   Email người nhận
     * @param userName  Tên người dùng
     * @param resetLink Link reset password
     */
    void sendPasswordResetEmail(String toEmail, String userName, String resetLink);
}
