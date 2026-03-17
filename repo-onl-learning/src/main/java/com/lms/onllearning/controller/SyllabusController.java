package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import com.lms.onllearning.service.ISyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/syllabuses")
@RequiredArgsConstructor
public class SyllabusController {

    private final ISyllabusService syllabusService;

    /**
     * Lấy danh sách tất cả syllabus từ CMS để hiển thị catalogue khóa học.
     * Public endpoint — không cần đăng nhập.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CmsEnvelope<List<SyllabusResponse>>>> getAllSyllabuses() {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.getAllSyllabuses()));
    }

    /**
     * Lấy chi tiết một syllabus kèm lịch học từ CMS.
     * Public endpoint — không cần đăng nhập.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CmsEnvelope<SyllabusDetailResponse>>> getSyllabusDetail(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.getSyllabusDetail(id)));
    }
}
