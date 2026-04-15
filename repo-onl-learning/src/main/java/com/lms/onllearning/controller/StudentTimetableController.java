package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
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

    /**
     * Lấy thời khóa biểu của học viên đã đăng nhập.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CmsEnvelope<List<StudentTimetableItemResponse>>>> getMyTimetable(
            Authentication authentication) {

        String email = extractEmail(authentication);

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "Không xác định được người dùng đăng nhập"));
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
}
