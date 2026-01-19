package com.lms.writing.service;

import com.lms.writing.dto.response.ExcelImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {

    ExcelImportResponse importFromExcel(
            MultipartFile file,
            String folderName,
            String description,
            boolean isPrivate,
            String userId);
}
