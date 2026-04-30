package com.lms.onllearning.service.impl;

import com.lms.onllearning.entity.LeadRegistration;
import com.lms.onllearning.entity.enums.RegistrationStatus;
import com.lms.onllearning.repository.LeadRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reconciler chạy sau mỗi lần sync timetable từ CMS.
 * <p>
 * Logic:
 * <ol>
 *   <li>PENDING + có thời khóa biểu → IN_PROGRESS</li>
 *   <li>PENDING + quá {@code lead.pending-expiry-days} ngày → CANCELED</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeadStatusReconciler {

    private final LeadRegistrationRepository leadRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${lead.pending-expiry-days:3}")
    private int pendingExpiryDays;

    /**
     * Chạy sau khi timetable sync hoàn tất.
     * Gọi từ {@link SystemSyncRunner} hoặc có thể gọi thủ công.
     */
    @Transactional
    public void reconcile() {
        log.info("[LeadReconciler] Bắt đầu reconcile lead statuses...");

        // 1. Lấy tất cả email có thời khóa biểu (student participants)
        Set<String> emailsWithTimetable = loadEmailsWithTimetable();

        // 2. PENDING + có timetable → IN_PROGRESS
        List<LeadRegistration> pendingLeads = leadRepository.findByStatus(RegistrationStatus.PENDING);
        int promotedCount = 0;
        for (LeadRegistration lead : pendingLeads) {
            String email = normalizeEmail(lead.getEmail());
            if (email != null && emailsWithTimetable.contains(email)) {
                lead.setStatus(RegistrationStatus.IN_PROGRESS);
                leadRepository.save(lead);
                promotedCount++;
                log.debug("[LeadReconciler] PENDING → IN_PROGRESS: leadId={}, email={}", lead.getId(), email);
            }
        }

        // 3. PENDING + quá hạn → CANCELED (bulk update)
        LocalDateTime cutoff = LocalDateTime.now().minusDays(pendingExpiryDays);
        int canceledCount = leadRepository.cancelExpiredPendingLeads(cutoff);

        log.info("[LeadReconciler] Reconcile hoàn tất. promoted={}, canceled={}", promotedCount, canceledCount);
    }

    private Set<String> loadEmailsWithTimetable() {
        String sql = "SELECT DISTINCT email FROM timetable_class_participants WHERE participant_role = 'STUDENT'";
        List<String> emails = jdbcTemplate.queryForList(sql, String.class);
        return emails.stream()
                .map(this::normalizeEmail)
                .filter(e -> e != null)
                .collect(Collectors.toSet());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
