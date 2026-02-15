package com.lms.quiz.util;

import com.lms.quiz.dto.excel.QuizExcelImportRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses Excel file for Quiz import.
 * 
 * Column layout:
 * A-B: Package (name, desc)
 * C-E: Subject (name, code, desc)
 * F-H: Slot (name, number, desc)
 * I-J: Folder (name, desc)
 * K: StudySet (name)
 * L-P: Quiz (title, desc, difficulty, timeLimit, passingScore)
 * Q-U: Question (type, text, explanation, points, difficulty)
 * V-Z: Data1-Data5 (type-specific)
 */
public class QuizExcelParser {

    private QuizExcelParser() {
    }

    /**
     * Parse Excel file into list of QuizExcelImportRow.
     * Skips the first row (header).
     */
    public static List<QuizExcelImportRow> parseFile(MultipartFile file) throws IOException {
        List<QuizExcelImportRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return rows;
            }

            Iterator<Row> rowIterator = sheet.iterator();
            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;

                QuizExcelImportRow importRow = parseRow(row, rowNum);
                rows.add(importRow);
            }
        }

        return rows;
    }

    private static QuizExcelImportRow parseRow(Row row, int rowNum) {
        return QuizExcelImportRow.builder()
                .rowNumber(rowNum)
                // Package (A-B)
                .packageName(getCellValue(row, 0))
                .packageDescription(getCellValue(row, 1))
                // Subject (C-E)
                .subjectName(getCellValue(row, 2))
                .subjectCode(getCellValue(row, 3))
                .subjectDescription(getCellValue(row, 4))
                // Slot (F-H)
                .slotName(getCellValue(row, 5))
                .slotNumber(getCellValue(row, 6))
                .slotDescription(getCellValue(row, 7))
                // Folder (I-J)
                .folderName(getCellValue(row, 8))
                .folderDescription(getCellValue(row, 9))
                // StudySet (K)
                .studySetName(getCellValue(row, 10))
                // Quiz (L-P)
                .quizTitle(getCellValue(row, 11))
                .quizDescription(getCellValue(row, 12))
                .quizDifficulty(getCellValue(row, 13))
                .timeLimitSeconds(getCellValue(row, 14))
                .passingScore(getCellValue(row, 15))
                // Question (Q-U)
                .questionType(getCellValue(row, 16))
                .questionText(getCellValue(row, 17))
                .explanation(getCellValue(row, 18))
                .points(getCellValue(row, 19))
                .questionDifficulty(getCellValue(row, 20))
                // Data1-Data5 (V-Z)
                .data1(getCellValue(row, 21))
                .data2(getCellValue(row, 22))
                .data3(getCellValue(row, 23))
                .data4(getCellValue(row, 24))
                .data5(getCellValue(row, 25))
                .build();
    }

    private static String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> {
                String value = cell.getStringCellValue();
                yield (value != null && !value.trim().isEmpty()) ? value.trim() : null;
            }
            case NUMERIC -> {
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    yield String.valueOf((long) numValue);
                }
                yield String.valueOf(numValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        double numValue = cell.getNumericCellValue();
                        if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                            yield String.valueOf((long) numValue);
                        }
                        yield String.valueOf(numValue);
                    } catch (Exception e2) {
                        yield null;
                    }
                }
            }
            default -> null;
        };
    }
}
