package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.onllearning.dto.response.SyncAllJobResponse;
import com.lms.onllearning.service.ISystemSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SystemSyncController {

    private final ISystemSyncService systemSyncService;

    @PostMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<SyncAllJobResponse>> startSyncAll() {
        SyncAllJobResponse job = systemSyncService.startSyncAll();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(job));
    }

    @GetMapping("/all/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<SyncAllJobResponse>> getSyncJob(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.ok(systemSyncService.getJob(jobId)));
    }

    @GetMapping("/all/current")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER_MANAGER')")
    public ResponseEntity<ApiResponse<SyncAllJobResponse>> getCurrentSyncJob() {
        return ResponseEntity.ok(ApiResponse.ok(systemSyncService.getCurrentJob()));
    }
}
