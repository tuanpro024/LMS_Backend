package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.dto.SyllabusTreeDTO;
import com.lms.videocourse.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/syllabus")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;

    /**
     * API to get the full Syllabus hierarchy.
     * Returns: list of packages, each containing its folders and study sets.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SyllabusTreeDTO>>> getAllSyllabus() {
        List<SyllabusTreeDTO> data = syllabusService.getFullSyllabusTree();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
