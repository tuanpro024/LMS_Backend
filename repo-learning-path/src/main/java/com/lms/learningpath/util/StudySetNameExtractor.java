package com.lms.learningpath.util;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Utility to extract studySetName from Excel sheet bytes.
 * 
 * CRITICAL: Parses minimal metadata (first non-empty StudySet Name cell)
 * to avoid memory overhead. It does NOT parse the entire sheet content.
 */
@Slf4j
public class StudySetNameExtractor {

    /**
     * Column index for studySetName in all module type Excel files.
     * Based on common format across all repos (Flashcard, Writing, Kanji,
     * Pronunciation, Quiz).
     */
    private static final int STUDY_SET_NAME_COLUMN = 10;

    /**
     * Row index for first data row (row 0 is header).
     */
    private static final int FIRST_DATA_ROW = 1;

    /**
     * Extract studySetName from sheetBytes for the given module type.
     * 
     * @param sheetBytes Excel sheet bytes (single-sheet workbook)
     * @param moduleType Type of module (for logging purposes)
     * @return Optional containing normalized studySetName if found, empty otherwise
     */
    public static Optional<String> extractStudySetName(byte[] sheetBytes, ModuleType moduleType) {
        if (sheetBytes == null || sheetBytes.length == 0) {
            log.debug("Cannot extract studySetName from null/empty bytes");
            return Optional.empty();
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(sheetBytes);
                Workbook workbook = new XSSFWorkbook(bis)) {

            // Get first sheet
            if (workbook.getNumberOfSheets() == 0) {
                log.warn("Workbook has no sheets");
                return Optional.empty();
            }

            Sheet sheet = workbook.getSheetAt(0);

            // Get first data row (skip header at row 0)
            Row dataRow = sheet.getRow(FIRST_DATA_ROW);
            if (dataRow == null) {
                log.warn("No data row found in sheet (moduleType: {})", moduleType);
                return Optional.empty();
            }

            // Get studySetName from column 0
            Cell cell = dataRow.getCell(STUDY_SET_NAME_COLUMN);
            String studySetName = getCellValueAsString(cell);

            if (studySetName == null || studySetName.isBlank()) {
                log.debug("StudySetName is null/blank in sheet (moduleType: {})", moduleType);
                return Optional.empty();
            }

            // Normalize studySetName
            String normalized = StringNormalizer.normalize(studySetName);
            log.debug("Extracted studySetName: '{}' (normalized: '{}') for moduleType: {}",
                    studySetName, normalized, moduleType);

            return Optional.of(normalized);

        } catch (IOException e) {
            log.error("Failed to extract studySetName from sheetBytes (moduleType: {}): {}",
                    moduleType, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get cell value as string, handling different cell types.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    // If it's a whole number, return without decimal
                    if (value == Math.floor(value)) {
                        yield String.valueOf((long) value);
                    } else {
                        yield String.valueOf(value);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    try {
                        yield String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        yield null;
                    }
                }
            }
            default -> null;
        };
    }
}
