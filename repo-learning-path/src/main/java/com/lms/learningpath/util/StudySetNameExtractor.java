package com.lms.learningpath.util;

import com.lms.learningpath.entity.enums.ModuleType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/**
 * Utility to extract studySetName from Excel sheet bytes.
 * 
 * CRITICAL: Parses minimal metadata (first non-empty StudySet Name cell)
 * to avoid memory overhead. It does NOT parse the entire sheet content.
 */
@Slf4j
public class StudySetNameExtractor {

    private static final String H_STUDY_SET_NAME = "studysetname";

    /**
     * Fallback column index for StudySetName when header is missing.
     * Common hierarchical template: PackageName..StudySetName (column E => index
     * 4).
     */
    private static final int FALLBACK_STUDY_SET_NAME_COLUMN = 4;

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

            int studySetNameColumn = resolveStudySetNameColumn(sheet.getRow(0));

            // Scan data rows and pick the first non-empty StudySetName.
            String studySetName = null;
            for (int rowIndex = FIRST_DATA_ROW; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);
                if (dataRow == null) {
                    continue;
                }

                Cell cell = dataRow.getCell(studySetNameColumn);
                String candidate = getCellValueAsString(cell);
                if (candidate != null && !candidate.isBlank()) {
                    studySetName = candidate;
                    break;
                }
            }

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

    private static int resolveStudySetNameColumn(Row headerRow) {
        if (headerRow == null) {
            return FALLBACK_STUDY_SET_NAME_COLUMN;
        }

        short firstCell = headerRow.getFirstCellNum();
        short lastCell = headerRow.getLastCellNum();
        if (firstCell < 0 || lastCell < 0) {
            return FALLBACK_STUDY_SET_NAME_COLUMN;
        }

        for (int i = firstCell; i < lastCell; i++) {
            String header = getCellValueAsString(headerRow.getCell(i));
            if (header == null) {
                continue;
            }

            if (H_STUDY_SET_NAME.equals(normalizeHeader(header))) {
                return i;
            }
        }

        return FALLBACK_STUDY_SET_NAME_COLUMN;
    }

    private static String normalizeHeader(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
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
