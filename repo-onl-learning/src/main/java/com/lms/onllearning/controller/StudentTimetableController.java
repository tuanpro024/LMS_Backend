package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
import com.lms.onllearning.entity.enums.RegistrationStatus;
import com.lms.onllearning.repository.LeadRegistrationRepository;
import com.lms.onllearning.service.ITimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({ "/students/me/timetable", "/users/me/timetable" })
@RequiredArgsConstructor
public class StudentTimetableController {

    private final ITimetableService timetableService;
    private final LeadRegistrationRepository leadRepository;

    /**
     * Lấy thời khóa biểu của học viên.
     *
     * Guard: user phải có ít nhất 1 lead với status = APPROVED mới được xem.
     * Nếu chưa APPROVED → 403 TIMETABLE_BLOCKED với thông báo rõ ràng.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CmsEnvelope<List<StudentTimetableItemResponse>>>> getMyTimetable(
            Authentication authentication) {

        String email = extractEmail(authentication);
        String userId = extractUserId(authentication);

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "Không xác định được người dùng đăng nhập"));
        }

        // ── Guard: chỉ cho xem timetable nếu có ít nhất 1 lead APPROVED ──────
        if (userId != null && !leadRepository.existsByUserIdAndStatus(userId, RegistrationStatus.APPROVED)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "TIMETABLE_BLOCKED",
                            "Bộ phận tư vấn chưa xác nhận đăng ký của bạn. " +
                            "Vui lòng chờ hoặc liên hệ với chúng tôi để được hỗ trợ."));
        }

        CmsEnvelope<List<StudentTimetableItemResponse>> result = timetableService.getStudentTimetable(email);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private String extractEmail(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof AuthPrincipal principal) {
            return principal.email();
        }
        return authentication.getName();
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof AuthPrincipal principal) {
            return principal.userId();
        }
        return null;
    }
}
