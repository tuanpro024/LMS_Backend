package com.lms.learningpath.service;

import com.lms.content.common.entity.TypeName;
import com.lms.learningpath.dto.excel.LearningPathImportResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for importing Learning Paths from Excel files using hybrid 2-phase
 * architecture.
 */
public interface LearningPathImportService {

    /**
     * Import Learning Path from Excel file.
     * 
     * Phase 1 (NO TRANSACTION): Create content in external repos via Feign
     * Phase 2 (LOCAL TRANSACTION): Build learning path hierarchy
     * 
     * @param file     Excel file with Structure sheet and content sheets
     * @param typeName TypeName enum for the Package Type
     * @param userId   User ID performing the import
     * @return Import result with IDs, counters, and errors
     */
    LearningPathImportResult importFromExcel(
            MultipartFile file,
            TypeName typeName,
            String userId);
}
