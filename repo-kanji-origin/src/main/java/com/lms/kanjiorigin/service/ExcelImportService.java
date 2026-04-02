package com.lms.kanjiorigin.service;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {

    /**
    * Import kanji origins từ file Excel với cấu trúc phân cấp đầy đủ
    * (Package -> Folder -> StudySet -> KanjiOrigin)
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
