package com.lms.quiz.service;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for importing quizzes from Excel files with hierarchical structure
 * (Package → Subject → Slot → Folder → StudySet → Quiz → Questions).
 */
public interface ExcelImportService {

    /**
     * Import quizzes from a hierarchical Excel file.
     *
     * @param file      Excel file
     * @param typeName  TypeName of the Package Type
     * @param userId    ID of the user performing the import
     * @param isPrivate Whether created items should be private
     * @return HierarchicalImportResult with import details
     */
    HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            com.lms.content.common.entity.TypeName typeName,
            String userId,
            boolean isPrivate);
}
