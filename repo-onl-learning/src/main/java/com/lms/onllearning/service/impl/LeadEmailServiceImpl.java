package com.lms.onllearning.service.impl;

import com.lms.onllearning.entity.LeadRegistration;
import com.lms.onllearning.service.ILeadEmailService;
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

/**
 * Email service cho lead registration.
 * Sử dụng Thymeleaf HTML template + JavaMailSender (giống pattern trong repo-identity).
 * @Async đảm bảo không block luồng chính khi gửi email.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeadEmailServiceImpl implements ILeadEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.email.from-name}")
    private String emailFromName;

    @Async
    @Override
    public void sendRegistrationConfirmationEmail(LeadRegistration lead) {
        try {
            log.info("Sending registration confirmation email to: {}", lead.getEmail());

            Context context = new Context();
            context.setVariable("fullName", lead.getFullName());
            context.setVariable("courseName", lead.getCourseName());
            context.setVariable("courseCode", lead.getCourseCode());
            context.setVariable("courseType", lead.getCourseType());

            String htmlContent = templateEngine.process("lead-registration-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(lead.getEmail());
            helper.setSubject("Xác nhận đăng ký tư vấn khóa học – HúLi Chinese");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Registration confirmation email sent to: {}", lead.getEmail());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send registration confirmation email to {}: {}", lead.getEmail(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending registration confirmation email to {}: {}", lead.getEmail(), e.getMessage());
        }
    }

    @Async
    @Override
    public void sendActivationEmail(LeadRegistration lead) {
        try {
            log.info("Sending activation email to: {}", lead.getEmail());

            Context context = new Context();
            context.setVariable("fullName", lead.getFullName());
            context.setVariable("courseName", lead.getCourseName());
            context.setVariable("courseCode", lead.getCourseCode());
            context.setVariable("approvedAt", lead.getApprovedAt());

            String htmlContent = templateEngine.process("lead-activation-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(lead.getEmail());
            helper.setSubject("🎉 Quyền học đã được kích hoạt – HúLi Chinese");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Activation email sent to: {}", lead.getEmail());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send activation email to {}: {}", lead.getEmail(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending activation email to {}: {}", lead.getEmail(), e.getMessage());
        }
    }
}
