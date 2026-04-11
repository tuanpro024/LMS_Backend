package com.lms.onllearning.service.impl;

import com.lms.onllearning.dto.request.LeadRegistrationRequest;
import com.lms.onllearning.dto.response.LeadRegistrationResponse;
import com.lms.onllearning.entity.LeadRegistration;
import com.lms.onllearning.mapper.LeadMapper;
import com.lms.onllearning.repository.LeadRegistrationRepository;
import com.lms.onllearning.service.ILeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadServiceImpl implements ILeadService {

    private final LeadRegistrationRepository repository;
    private final LeadMapper mapper;

    @Override
    @Transactional
    public LeadRegistrationResponse register(LeadRegistrationRequest request, String userId) {
        // Idempotency: nếu đã đăng ký cùng course code, trả về lead cũ
        Optional<LeadRegistration> existing = repository.findByUserIdAndCourseCode(userId, request.code());

        if (existing.isPresent()) {
            log.info("Duplicate lead registration skipped for userId={}, courseCode={}",
                    userId, request.code());
            return mapper.toResponse(existing.get());
        }

        // Tạo lead mới
        LeadRegistration lead = mapper.toEntity(request);
        lead.setId(generateId());
        lead.setUserId(userId); // userId từ JWT, không từ client

        LeadRegistration saved = repository.save(lead);
        log.info("Lead registered: id={}, userId={}, courseCode={}",
                saved.getId(), userId, request.code());

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
    public List<LeadRegistrationResponse> getAllLeads(
            String courseCode,
            LocalDate from,
            LocalDate to) {

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;

        return repository.findWithFilters(courseCode, fromDt, toDt, Pageable.unpaged())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Tạo ULID-compatible ID (26 chars) dùng UUID đơn giản */
    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 26);
    }
}
