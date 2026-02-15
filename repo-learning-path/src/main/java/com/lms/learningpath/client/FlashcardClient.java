package com.lms.learningpath.client;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.TypeName;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Unified Feign client for repo-flashcard service.
 * Handles both Excel import and regular service operations.
 */
@FeignClient(name = "repo-flashcard", contextId = "flashcard", configuration = com.lms.learningpath.config.FeignConfig.class)
public interface FlashcardClient {

    // ==================== Import Operations ====================

    /**
     * Import flashcard content from Excel file
     */
    @PostMapping(value = "/packages/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<HierarchicalImportResult> importExcel(
            @RequestPart("file") MultipartFile file,
            @RequestParam("typeName") TypeName typeName);

    // ==================== Service Operations ====================

    /**
     * Get study set by ID
     */
    @GetMapping("/study-sets/{id}")
    ApiResponse<StudySetResponse> getStudySetById(@PathVariable("id") String id);

    /**
     * Get all study sets
     */
    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> getAllStudySets();

    /**
     * Search study sets by query
     */
    @GetMapping("/study-sets")
    ApiResponse<List<StudySetResponse>> searchStudySets(@RequestParam("q") String query);

    /**
     * Find study sets by exact title and user ID (case-insensitive)
     */
    @GetMapping("/study-sets/exact-match")
    ApiResponse<List<StudySetResponse>> findByTitleAndUserIdIgnoreCase(
            @RequestParam("title") String title,
            @RequestParam("userId") String userId);
}
