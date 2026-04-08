package com.lms.identity.controller;

import com.lms.identity.dto.request.AdminCreateUserRequest;
import com.lms.identity.dto.request.AdminUpdateUserRequest;
import com.lms.identity.dto.response.AdminUserResponse;
import com.lms.identity.service.TeacherService;
import com.lms.identity.service.TeacherSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherSyncService teacherSyncService;

    @GetMapping

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<List<AdminUserResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<AdminUserResponse> createTeacher(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.ok(teacherService.createTeacher(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<AdminUserResponse> updateTeacher(@PathVariable String id, @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<Void> blockTeacher(@PathVariable String id) {
        teacherService.blockTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER_MANAGER')")
    public ResponseEntity<String> syncFromCms() {
        teacherSyncService.syncTeachers();
        return ResponseEntity.ok("Sync process triggered successfully");
    }
}
