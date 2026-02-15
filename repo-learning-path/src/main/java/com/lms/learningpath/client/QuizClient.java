package com.lms.learningpath.client;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.learningpath.dto.request.quiz.CreateQuizRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Unified Feign client for repo-quiz service.
 * Handles both Excel import, JSON-based quiz creation, and regular service
 * operations.
 */
@FeignClient(name = "repo-quiz", contextId = "quiz", configuration = com.lms.learningpath.config.FeignConfig.class)
public interface QuizClient {

    // ==================== Import Operations ====================

    /**
     * Import quiz content from Excel file (hierarchical structure)
     */
    @PostMapping(value = "/packages/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<HierarchicalImportResult> importExcel(
            @RequestPart("file") MultipartFile file,
            @RequestParam("typeName") TypeName typeName);

    /**
     * Create quiz from JSON request (direct API)
     * Alternative to Excel import for programmatic quiz creation
     */
    @PostMapping(value = "/study-sets", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<StudySetResponse> createQuiz(@RequestBody CreateQuizRequest request);

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
