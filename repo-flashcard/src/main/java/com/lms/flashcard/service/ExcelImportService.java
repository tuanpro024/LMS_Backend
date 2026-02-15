package com.lms.flashcard.service;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.flashcard.dto.response.ExcelImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {

    /**
     * Import flashcards từ file Excel với cấu trúc phân cấp đầy đủ
     * (Package -> Subject -> Slot -> Folder -> StudySet -> Cards)
     *
     * @param file      Excel file
     * @param typeName  TypeName của Package Type
     * @param userId    ID của user
     * @param isPrivate Các items có private không
     * @return HierarchicalImportResult chứa thông tin import
     */
    HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            com.lms.content.common.entity.TypeName typeName,
            String userId,
            boolean isPrivate);
}
