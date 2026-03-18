package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
import com.lms.onllearning.service.ITimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/students/me/timetable")
@RequiredArgsConstructor
public class StudentTimetableController {

    private final ITimetableService timetableService;

    @GetMapping
    public ResponseEntity<ApiResponse<CmsEnvelope<List<StudentTimetableItemResponse>>>> getMyTimetable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication) {

        if (end.isBefore(start)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_DATE_RANGE", "Tham số end phải lớn hơn hoặc bằng start"));
        }

        String studentId = extractStudentId(authentication);
        if (studentId == null || studentId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "Không xác định được người dùng đăng nhập"));
        }

        CmsEnvelope<List<StudentTimetableItemResponse>> result =
                timetableService.getStudentTimetable(studentId, start, end);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private String extractStudentId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof AuthPrincipal principal) {
            return principal.userId();
        }
        return authentication.getName();
    }
}
