package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.service.IOnlineCourseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class OnlineCourseSyncController {

    private final IOnlineCourseSyncService syncService;

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<String>> syncAll() {
        syncService.syncAll();
        return ResponseEntity.ok(ApiResponse.ok("Dong bo tat ca online courses tu CMS thanh cong"));
    }

    @PostMapping("/sync/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<String>> syncByType(@PathVariable String type) {
        syncService.syncByType(type);
        return ResponseEntity.ok(ApiResponse.ok("Dong bo online courses type=" + type + " thanh cong"));
    }
}
