package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.TimetableResponse;
import com.lms.onllearning.service.ITimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/onl/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final ITimetableService timetableService;

    /**
     * Xem thời khóa biểu cá nhân.
     * studentId = userId từ JWT — CMS sẽ mapping sẵn khi xếp lịch.
     *
     * Response meta:
     *  - meta.scheduled = false → CMS trả 404, học viên chưa có lịch
     *  - meta.isStale = true    → CMS down, dữ liệu từ cache
     *  - meta.cmsUnavailable    → CMS down và không có cache
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<CmsEnvelope<List<TimetableResponse>>>> getMyTimetable(
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getMyTimetable(userId)));
    }
}
