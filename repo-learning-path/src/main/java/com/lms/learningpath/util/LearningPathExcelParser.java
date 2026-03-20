package com.lms.learningpath.util;

import com.lms.learningpath.dto.excel.LearningPathExcelData;
import com.lms.learningpath.dto.excel.LearningPathStructureRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Memory-safe parser for Learning Path Excel files.
 * 
 * CRITICAL: This parser extracts content sheets as byte[] to avoid
 * memory leaks and Sheet object lifecycle issues after Workbook closure.
 */
@Slf4j
public class LearningPathExcelParser {

    // Column indices for Structure sheet
    private static final int COL_PACKAGE_NAME = 0; // A
    private static final int COL_PACKAGE_DESC = 1; // B
    private static final int COL_FOLDER_NAME = 2; // C
    private static final int COL_FOLDER_DESC = 3; // D
    private static final int COL_STUDY_SET_NAME = 4; // E
    private static final int COL_STUDY_SET_DESC = 5; // F
    private static final int COL_LP_TITLE = 6; // G
    private static final int COL_LP_DESC = 7; // H
    private static final int COL_STEP_TITLE = 8; // I
    private static final int COL_STEP_DESC = 9; // J
    private static final int COL_STEP_ORDER = 10; // K
    private static final int COL_MODULE_TITLE = 11; // L
    private static final int COL_MODULE_TYPE = 12; // M
    private static final int COL_MODULE_ORDER = 13; // N
    private static final int COL_IS_REQUIRED = 14; // O
    private static final int COL_CONTENT_SHEET_NAME = 15; // P

    // Header markers to detect Structure sheet accidentally used as content sheet
    private static final String HEADER_MODULE_TYPE = "ModuleType";
    private static final String HEADER_CONTENT_SHEET_NAME = "ContentSheetName";
    private static final String HEADER_SUBJECT_NAME = "SubjectName";

    /**
     * Parse multi-sheet Excel file.
     * 
     * @param file Excel file
     * @return LearningPathExcelData with structure rows and content sheet bytes
     * @throws IOException if parsing fails
     */
    public static LearningPathExcelData parseMultiSheet(MultipartFile file) throws IOException {
        validateFile(file);

        List<LearningPathStructureRow> structureRows = new ArrayList<>();
        Map<String, byte[]> contentSheetBytes = new LinkedHashMap<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            // (1) Parse "Structure" sheet (first sheet or sheet with name "Structure")
            Sheet structureSheet = findStructureSheet(workbook);
            if (structureSheet == null) {
                throw new IllegalArgumentException("Structure sheet not found in workbook");
            }
            structureRows = parseStructureSheet(structureSheet);

            // (2) Collect required content sheet names from structure rows
            Set<String> requiredSheetNames = new LinkedHashSet<>();
            for (LearningPathStructureRow row : structureRows) {
                if (row.getContentSheetName() != null && !row.getContentSheetName().isBlank()) {
                    requiredSheetNames.add(row.getContentSheetName().trim());
                }
            }

            // (3) Extract each content sheet to byte[] (memory-safe)
            for (String sheetName : requiredSheetNames) {
                Sheet contentSheet = workbook.getSheet(sheetName);
                if (contentSheet == null) {
                    log.warn("Content sheet '{}' not found in workbook. Skipping.", sheetName);
                    continue;
                }

                // Defensive guard: never allow Structure schema to be used as content sheet
                if (isLikelyStructureSheet(contentSheet)) {
                    throw new IllegalArgumentException(
                            "Sheet '" + sheetName + "' matches Structure schema and cannot be used as content sheet");
                }

                try {
                    byte[] sheetBytes = extractSheetToBytes(contentSheet);
                    contentSheetBytes.put(sheetName, sheetBytes);
                    log.debug("Extracted content sheet '{}' ({} bytes)", sheetName, sheetBytes.length);
                } catch (Exception e) {
                    log.error("Failed to extract sheet '{}': {}", sheetName, e.getMessage());
                    // Continue with other sheets
                }
            }

        } // Workbook closes here - all Sheet objects invalidated, but byte[] data is safe

        log.info("Parsed Excel file: {} structure rows, {} content sheets",
                structureRows.size(), contentSheetBytes.size());

        return new LearningPathExcelData(structureRows, contentSheetBytes);
    }

    /**
     * Find the Structure sheet in the workbook.
     * Tries: sheet named "Structure" first, then falls back to first sheet.
     */
    private static Sheet findStructureSheet(Workbook workbook) {
        Sheet structureSheet = workbook.getSheet("Structure");
        if (structureSheet != null) {
            return structureSheet;
        }

        // Fallback to first sheet
        if (workbook.getNumberOfSheets() > 0) {
            return workbook.getSheetAt(0);
        }

        return null;
    }

    /**
     * Identify whether a sheet appears to follow the Learning Path Structure
     * schema.
     *
     * Structure header contains ModuleType + ContentSheetName, while content sheets
     * are expected to use SubjectName in the third column.
     */
    private static boolean isLikelyStructureSheet(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return false;
        }

        String colC = normalizeHeader(getCellValue(headerRow, COL_FOLDER_NAME)); // index 2
        String colM = normalizeHeader(getCellValue(headerRow, COL_MODULE_TYPE)); // index 12
        String colP = normalizeHeader(getCellValue(headerRow, COL_CONTENT_SHEET_NAME)); // index 15

        boolean hasStructureMarkers = HEADER_MODULE_TYPE.equalsIgnoreCase(colM)
                && HEADER_CONTENT_SHEET_NAME.equalsIgnoreCase(colP);
        boolean looksLikeContentTemplate = HEADER_SUBJECT_NAME.equalsIgnoreCase(colC);

        return hasStructureMarkers && !looksLikeContentTemplate;
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Parse Structure sheet into LearningPathStructureRow list.
     */
    private static List<LearningPathStructureRow> parseStructureSheet(Sheet sheet) {
        List<LearningPathStructureRow> rows = new ArrayList<>();

        int lastRowNum = sheet.getLastRowNum();
        log.debug("Parsing Structure sheet: {} rows", lastRowNum);

        // Skip header row (row 0)
        for (int i = 1; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            LearningPathStructureRow structRow = parseStructureRow(row, i + 1);
            rows.add(structRow);
        }

        return rows;
    }

    /**
     * Parse a single Structure row.
     */
    private static LearningPathStructureRow parseStructureRow(Row row, int rowNumber) {
        return LearningPathStructureRow.builder()
                .rowNumber(rowNumber)
                // Package (A-B)
                .packageName(getCellValue(row, COL_PACKAGE_NAME))
                .packageDescription(getCellValue(row, COL_PACKAGE_DESC))
                // Folder (C-D)
                .folderName(getCellValue(row, COL_FOLDER_NAME))
                .folderDescription(getCellValue(row, COL_FOLDER_DESC))
                // StudySet (E-F)
                .studySetName(getCellValue(row, COL_STUDY_SET_NAME))
                .studySetDescription(getCellValue(row, COL_STUDY_SET_DESC))
                // LearningPath (G-H)
                .learningPathTitle(getCellValue(row, COL_LP_TITLE))
                .learningPathDescription(getCellValue(row, COL_LP_DESC))
                // Step (I-K)
                .stepTitle(getCellValue(row, COL_STEP_TITLE))
                .stepDescription(getCellValue(row, COL_STEP_DESC))
                .stepOrder(parseInteger(getCellValue(row, COL_STEP_ORDER)))
                // Module (L-Q)
                .moduleTitle(getCellValue(row, COL_MODULE_TITLE))
                .moduleType(getCellValue(row, COL_MODULE_TYPE))
                .moduleOrder(parseInteger(getCellValue(row, COL_MODULE_ORDER)))
                .isRequired(parseBoolean(getCellValue(row, COL_IS_REQUIRED)))
                .contentSheetName(getCellValue(row, COL_CONTENT_SHEET_NAME))
                .build();
    }

    /**
     * Extract a single sheet to byte[] by copying it to a new workbook.
     * This is memory-safe and allows the original workbook to be closed.
     * 
     * @param sourceSheet Sheet to extract
     * @return byte[] of workbook containing only this sheet
     */
    private static byte[] extractSheetToBytes(Sheet sourceSheet) throws IOException {
        try (Workbook newWorkbook = new XSSFWorkbook()) {
            Sheet newSheet = newWorkbook.createSheet(sourceSheet.getSheetName());

            // Copy all rows and cells
            int lastRowNum = sourceSheet.getLastRowNum();
            for (int i = 0; i <= lastRowNum; i++) {
                Row sourceRow = sourceSheet.getRow(i);
                if (sourceRow == null)
                    continue;

                Row newRow = newSheet.createRow(i);

                short lastCellNum = sourceRow.getLastCellNum();
                for (int j = 0; j < lastCellNum; j++) {
                    Cell sourceCell = sourceRow.getCell(j);
                    if (sourceCell == null)
                        continue;

                    Cell newCell = newRow.createCell(j, sourceCell.getCellType());
                    copyCellValue(sourceCell, newCell);
                }
            }

            // Serialize to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            newWorkbook.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * Copy cell value from source to target.
     */
    private static void copyCellValue(Cell source, Cell target) {
        switch (source.getCellType()) {
            case STRING -> target.setCellValue(source.getStringCellValue());
            case NUMERIC -> target.setCellValue(source.getNumericCellValue());
            case BOOLEAN -> target.setCellValue(source.getBooleanCellValue());
            case FORMULA -> {
                try {
                    target.setCellFormula(source.getCellFormula());
                } catch (Exception e) {
                    // If formula copy fails, try to copy evaluated value
                    try {
                        target.setCellValue(source.getNumericCellValue());
                    } catch (Exception e2) {
                        target.setCellValue(source.getStringCellValue());
                    }
                }
            }
            case BLANK -> target.setBlank();
            default -> {
            }
        }
    }

    /**
     * Convert byte[] to MockMultipartFile for Feign upload.
     * 
     * @param bytes     Sheet workbook bytes
     * @param sheetName Original sheet name (used for filename)
     * @return MultipartFile ready to upload
     */
    public static MultipartFile bytesToMultipartFile(byte[] bytes, String sheetName) {
        return new MockMultipartFile(
                "file",
                sheetName + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bytes);
    }

    /**
     * Get cell value as string (reuses logic from HierarchicalExcelParser).
     */
    private static String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
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

    /**
     * Parse string to Integer.
     */
    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse string to Boolean.
     */
    private static Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized);
    }

    /**
     * Validate file format.
     */
    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is null");
        }

        if (!filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Invalid file format. Only .xlsx is supported");
        }
    }
}
