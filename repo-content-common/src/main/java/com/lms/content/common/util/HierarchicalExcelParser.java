package com.lms.content.common.util;

import com.lms.content.common.dto.excel.HierarchicalImportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for hierarchical Excel structure
 * Format: Package -> Subject -> Slot -> Folder -> StudySet -> Content Items
 */
public class HierarchicalExcelParser {

    private static final int COL_PACKAGE_NAME = 0; // A
    private static final int COL_PACKAGE_DESCRIPTION = 1; // B
    private static final int COL_SUBJECT_NAME = 2; // C
    private static final int COL_SUBJECT_CODE = 3; // D
    private static final int COL_SUBJECT_DESCRIPTION = 4; // E
    private static final int COL_SLOT_NAME = 5; // F
    private static final int COL_SLOT_NUMBER = 6; // G
    private static final int COL_SLOT_DESCRIPTION = 7; // H
    private static final int COL_FOLDER_NAME = 8; // I
    private static final int COL_FOLDER_DESCRIPTION = 9; // J
    private static final int COL_STUDY_SET_NAME = 10; // K
    private static final int COL_STUDY_SET_DESCRIPTION = 11; // L
    private static final int COL_TERM = 12; // M
    private static final int COL_DEFINITION = 13; //N
    private static final int COL_PINYIN = 14; // 0
    private static final int COL_SINO_VN = 15; // P
    private static final int COL_WORD_TYPE = 16; // Q
    private static final int COL_HSK_LEVEL = 17; // R
    private static final int COL_IMAGE_WORD = 18; // S
    private static final int COL_SINO_ORIGIN = 19; // T
    private static final int COL_IMAGE_ORIGIN = 20; // U
    private static final int COL_AUDIO = 21; // V
    private static final int COL_EXAMPLE_SENTENCE = 22; // W
    private static final int COL_EXAMPLE_PINYIN = 23; // X
    private static final int COL_EXAMPLE_MEANING = 24; // Y
    private static final int COL_CHARACTERS_JSON = 25; // Z

    /**
     * Parse Excel file into hierarchical rows
     */
    public static List<HierarchicalImportRow> parseFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is null");
        }

        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            throw new IllegalArgumentException("Invalid file format. Only .xlsx and .xls are supported");
        }

        try (InputStream is = file.getInputStream()) {
            return parseExcel(is);
        }
    }

    /**
     * Parse Excel input stream
     */
    private static List<HierarchicalImportRow> parseExcel(InputStream is) throws IOException {
        List<HierarchicalImportRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row (row 0)
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                HierarchicalImportRow importRow = parseRow(row, i + 1);
                rows.add(importRow);
            }
        }

        return rows;
    }

    /**
     * Parse a single row
     */
    private static HierarchicalImportRow parseRow(Row row, int rowNumber) {
        return HierarchicalImportRow.builder()
                .rowNumber(rowNumber)
                // Package level
                .packageName(getCellValue(row, COL_PACKAGE_NAME))
                .packageDescription(getCellValue(row, COL_PACKAGE_DESCRIPTION))
                // Subject level
                .subjectName(getCellValue(row, COL_SUBJECT_NAME))
                .subjectCode(getCellValue(row, COL_SUBJECT_CODE))
                .subjectDescription(getCellValue(row, COL_SUBJECT_DESCRIPTION))
                // Slot level
                .slotName(getCellValue(row, COL_SLOT_NAME))
                .slotNumber(getCellValue(row, COL_SLOT_NUMBER))
                .slotDescription(getCellValue(row, COL_SLOT_DESCRIPTION))
                // Folder level
                .folderName(getCellValue(row, COL_FOLDER_NAME))
                .folderDescription(getCellValue(row, COL_FOLDER_DESCRIPTION))
                // StudySet level
                .studySetName(getCellValue(row, COL_STUDY_SET_NAME))
                .studySetDescription(getCellValue(row, COL_STUDY_SET_DESCRIPTION))
                // Content items
                .term(getCellValue(row, COL_TERM))
                .definition(getCellValue(row, COL_DEFINITION))
                .pinyin(getCellValue(row, COL_PINYIN))
                .sinoVn(getCellValue(row, COL_SINO_VN))
                .wordType(getCellValue(row, COL_WORD_TYPE))
                .hskLevel(getCellValue(row, COL_HSK_LEVEL))
                .imageWord(getCellValue(row, COL_IMAGE_WORD))
                .sinoOrigin(getCellValue(row, COL_SINO_ORIGIN))
                .imageOrigin(getCellValue(row, COL_IMAGE_ORIGIN))
                .audio(getCellValue(row, COL_AUDIO))
                .exampleSentence(getCellValue(row, COL_EXAMPLE_SENTENCE))
                .examplePinyin(getCellValue(row, COL_EXAMPLE_PINYIN))
                .exampleMeaning(getCellValue(row, COL_EXAMPLE_MEANING))
                .charactersJson(getCellValue(row, COL_CHARACTERS_JSON))
                .build();
    }

    /**
     * Get cell value as string
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
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }
}
