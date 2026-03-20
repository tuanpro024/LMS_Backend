package com.lms.flashcard.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.flashcard.exception.InvalidFileFormatException;
import com.lms.flashcard.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {
    private final FlashcardHierarchicalImportService hierarchicalImportService;

    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file,
            com.lms.content.common.entity.TypeName typeName,
            String userId,
            boolean isPrivate) {
        log.info("Starting hierarchical Excel import for user: {} with typeName: {}", userId, typeName);

        validateNotLearningPathWorkbook(file);

        return hierarchicalImportService.importFromPackageExcel(file, typeName, userId, isPrivate);
    }

    private void validateNotLearningPathWorkbook(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileFormatException("File is empty or null");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            return;
        }

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getSheet("Structure") != null) {
                throw new InvalidFileFormatException(
                        "Workbook contains sheet 'Structure'. Please import this file via /learning-paths/import-excel endpoint.");
            }
        } catch (InvalidFileFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new InvalidFileFormatException("Failed to inspect uploaded Excel file", e);
        }
    }

}
