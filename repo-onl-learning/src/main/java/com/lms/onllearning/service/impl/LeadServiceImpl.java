package com.lms.onllearning.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.common.notification.NotificationEvent;
import com.lms.common.notification.NotificationPublisher;
import com.lms.common.notification.ResourceType;
import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import com.lms.onllearning.entity.LeadRegistration;
import com.lms.onllearning.entity.enums.RegistrationStatus;
import com.lms.onllearning.mapper.LeadMapper;
import com.lms.onllearning.repository.LeadRegistrationRepository;
import com.lms.onllearning.service.ILeadEmailService;
import com.lms.onllearning.service.ILeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadServiceImpl implements ILeadService {

    private final LeadRegistrationRepository repository;
    private final LeadMapper mapper;
    private final NotificationPublisher notificationPublisher;
    private final ILeadEmailService leadEmailService;

    @Qualifier("emailTaskExecutor")
    private final Executor emailTaskExecutor;

    @Override
    @Transactional
    public LeadRegistrationResponse register(LeadRegistrationRequest request, String userId) {
        Optional<LeadRegistration> existing = repository.findByUserIdAndCourseCode(userId, request.code());

        if (existing.isPresent()) {
            LeadRegistration lead = existing.get();

            // Nếu đã hoàn thành hoặc bị hủy → cho phép đăng ký lại (reset bản ghi cũ)
            if (lead.getStatus() == RegistrationStatus.COMPLETED
                    || lead.getStatus() == RegistrationStatus.CANCELED) {
                log.info("Re-registration (status={}) for userId={}, courseCode={}",
                        lead.getStatus(), userId, request.code());

                lead.setFullName(request.fullName());
                lead.setEmail(request.email());
                lead.setPhone(request.phone());
                lead.setNote(request.note());
                lead.setCourseName(request.name());
                lead.setCourseType(request.courseType());
                lead.setStatus(RegistrationStatus.PENDING);
                lead.setCompletedAt(null);

                LeadRegistration saved = repository.save(lead);

                // Gửi notification + email ASYNC (không block response)
                submitPostRegistrationTasks(saved);

                return mapper.toResponse(saved);
            }

            // Idempotency: nếu đang PENDING hoặc IN_PROGRESS, trả về lead cũ
            log.info("Duplicate lead registration skipped for userId={}, courseCode={}",
                    userId, request.code());
            return mapper.toResponse(lead);
        }

        // Tạo lead mới — status mặc định PENDING (@Builder.Default)
        LeadRegistration lead = mapper.toEntity(request);
        lead.setId(generateId());
        lead.setUserId(userId); // userId từ JWT, không từ client

        LeadRegistration saved = repository.save(lead);
        log.info("Lead registered: id={}, userId={}, courseCode={}, status={}",
                saved.getId(), userId, request.code(), saved.getStatus());

        // Gửi notification + email ASYNC (không block response)
        submitPostRegistrationTasks(saved);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadRegistrationResponse> getLeads(
            String courseCode,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;

        return repository.findWithFilters(courseCode, fromDt, toDt, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadRegistrationResponse getMyRegistration(String userId, String courseCode) {
        return repository.findByUserIdAndCourseCode(userId, courseCode)
                .map(mapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public void markCourseCompleted(String userId, String courseCode) {
        LeadRegistration lead = repository.findByUserIdAndCourseCode(userId, courseCode)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.E227,
                        "Không tìm thấy đăng ký cho khóa học: " + courseCode,
                        HttpStatus.NOT_FOUND));

        if (lead.getStatus() == RegistrationStatus.COMPLETED) {
            log.info("Lead already COMPLETED for userId={}, courseCode={}", userId, courseCode);
            return;
        }

        lead.setStatus(RegistrationStatus.COMPLETED);
        lead.setCompletedAt(LocalDateTime.now());
        repository.save(lead);

        log.info("Lead marked COMPLETED: id={}, userId={}, courseCode={}",
                lead.getId(), userId, courseCode);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /**
     * Submit post-registration tasks (notification + email) to async executor.
     * ✅ API response trả về ngay lập tức (transaction đã commit)
     * ✅ Tasks chạy background không block HTTP response
     */
    private void submitPostRegistrationTasks(LeadRegistration lead) {
        emailTaskExecutor.execute(() -> {
            try {
                publishRegistrationConfirmation(lead);
                leadEmailService.sendRegistrationConfirmationEmail(lead);
            } catch (Exception ex) {
                log.error("Error in post-registration tasks for lead {}: {}", lead.getId(), ex.getMessage(), ex);
            }
        });
    }

    private void publishRegistrationConfirmation(LeadRegistration lead) {
        try {
            NotificationEvent event = new NotificationEvent(
                    lead.getUserId(),
                    "LEAD_REGISTRATION_PENDING",
                    "Đăng ký nhận tư vấn thành công",
                    "Chúng tôi đã nhận đăng ký của bạn và chuyển thông tin sang hệ thống quản lý.",
                    ResourceType.OTHER,
                    lead.getId(),
                    Map.of("courseCode", lead.getCourseCode(), "courseName", lead.getCourseName()),
                    "lead-reg-" + lead.getId());
            notificationPublisher.publish(event);
        } catch (Exception ex) {
            log.warn("Failed to publish registration confirmation notification for lead {}: {}",
                    lead.getId(), ex.getMessage());
        }
    }

    /** Tạo ULID-compatible ID (26 chars) dùng UUID đơn giản */
    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 26);
    }
}
