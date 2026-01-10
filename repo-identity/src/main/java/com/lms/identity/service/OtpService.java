package com.lms.identity.service;

import com.lms.identity.entity.User;

public interface OtpService {
    /**
     * Generate và gửi OTP cho user qua email
     * 
     * @param user User cần gửi OTP
     * @return OTP token value
     */
    String generateAndSendOtp(User user);

    /**
     * Verify OTP và set emailVerified = true
     * 
     * @param email Email của user
     * @param otp   OTP code (6 digits)
     * @return User đã được verify
     */
    User verifyOtp(String email, String otp);
}
