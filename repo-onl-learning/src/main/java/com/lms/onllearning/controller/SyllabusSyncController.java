package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.service.ISyllabusSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/syllabus")
@RequiredArgsConstructor
public class SyllabusSyncController {

    private final ISyllabusSyncService syncService;

    /**
     * Trigger đồng bộ toàn bộ syllabus từ CMS vào DB.
     * Chỉ ADMIN mới được phép.
     */
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<String>> syncAll() {
        syncService.syncAll();
        return ResponseEntity.ok(ApiResponse.ok("Đồng bộ tất cả syllabus thành công"));
    }

    /**
     * Trigger đồng bộ 1 syllabus theo id từ CMS vào DB.
     */
    @PostMapping("/sync/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<String>> syncOne(@PathVariable String id) {
        syncService.syncOne(id);
        return ResponseEntity.ok(ApiResponse.ok("Đồng bộ syllabus id=" + id + " thành công"));
    }
}
