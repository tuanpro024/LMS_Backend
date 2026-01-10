package com.lms.flashcard.service;

import com.lms.flashcard.dto.response.ExcelImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {

    /**
     * Import flashcards từ file Excel hoặc CSV
     *
     * @param file        Excel/CSV file
     * @param folderName  Tên folder chứa các study sets
     * @param description Mô tả folder (optional)
     * @param isPrivate   Folder có private không
     * @param userId      ID của user
     * @return ExcelImportResponse chứa thông tin import
     */
    ExcelImportResponse importFromExcel(
            MultipartFile file,
            String folderName,
            String description,
            boolean isPrivate,
            String userId);
}
