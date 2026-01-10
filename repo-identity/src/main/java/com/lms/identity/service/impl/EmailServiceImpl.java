package com.lms.identity.service.impl;

import com.lms.identity.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.email.from-name}")
    private String emailFromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String userName, String verificationLink) {
        try {
            log.info("Sending verification email to: {}", toEmail);

            // Tạo context cho Thymeleaf template
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("frontendUrl", frontendUrl);

            // Render HTML từ template
            String htmlContent = templateEngine.process("verification-email", context);

            // Tạo email message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(toEmail);
            helper.setSubject("Xác Thực Tài Khoản LMS - Verification Email");
            helper.setText(htmlContent, true); // true = HTML content

            // Gửi email
            mailSender.send(message);

            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Could not send verification email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", toEmail, e);
            throw new RuntimeException("Unexpected error sending email", e);
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        try {
            log.info("Sending password reset email to: {}", toEmail);

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("password-reset-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(toEmail);
            helper.setSubject("Đặt Lại Mật Khẩu LMS - Password Reset");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Could not send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", toEmail, e);
            throw new RuntimeException("Unexpected error sending email", e);
        }
    }

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String userName, String otp) {
        try {
            log.info("Sending OTP email to: {}", toEmail);

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("otp", otp);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("otp-verification-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(toEmail);
            helper.setSubject("Mã Xác Thực OTP - LMS");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Could not send OTP email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending OTP email to: {}", toEmail, e);
            throw new RuntimeException("Unexpected error sending OTP email", e);
        }
    }
}
