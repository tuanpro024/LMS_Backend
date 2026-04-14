package com.lms.notification.service.Impl;

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
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.email.from-name}")
    private String emailFromName;

    @Value("${app.frontend.url:https://lms.dangch.tech}")
    private String appUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Async
    public void sendPurchaseSuccessEmail(String email, String fullName, java.time.Instant expiryDate) {
        try {
            log.info("Sending premium purchase success email to: {}", email);

            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("expiryDate", DATE_FORMATTER.format(java.time.OffsetDateTime.ofInstant(expiryDate, java.time.ZoneId.systemDefault())));
            context.setVariable("appUrl", appUrl);

            String htmlContent = templateEngine.process("premium-purchase-success", context);

            sendEmail(email, "👑 Nâng Cấp Premium Thành Công – Học Bá", htmlContent);
        } catch (Exception e) {
            log.error("Failed to send purchase success email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendExpiryWarningEmail(String email, String fullName, java.time.Instant expiryDate, int daysRemaining) {
        try {
            log.info("Sending premium expiry warning email to: {}", email);

            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("expiryDate", DATE_FORMATTER.format(java.time.OffsetDateTime.ofInstant(expiryDate, java.time.ZoneId.systemDefault())));
            context.setVariable("daysRemaining", daysRemaining);
            context.setVariable("renewUrl", appUrl + "/membership");

            String htmlContent = templateEngine.process("premium-expiry-warning", context);

            sendEmail(email, "⚠️ Thông Báo Sắp Hết Hạn Premium – Học Bá", htmlContent);
        } catch (Exception e) {
            log.error("Failed to send expiry warning email to {}: {}", email, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(emailFrom, emailFromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
