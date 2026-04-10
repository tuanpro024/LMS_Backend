package com.lms.identity.controller;

import com.lms.identity.dto.response.ExternalTeacherPublicResponse;
import com.lms.identity.service.ExternalTeacherPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/teachers")
@RequiredArgsConstructor
public class PublicTeacherController {

    private final ExternalTeacherPublicService externalTeacherPublicService;

    @GetMapping
    public ResponseEntity<List<ExternalTeacherPublicResponse>> getAllTeachers() {
        return ResponseEntity.ok(externalTeacherPublicService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalTeacherPublicResponse> getTeacherById(@PathVariable String id) {
        return ResponseEntity.ok(externalTeacherPublicService.getTeacherById(id));
    }
}
