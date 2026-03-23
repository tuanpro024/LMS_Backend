package com.lms.quiz.util;

import com.lms.quiz.dto.excel.QuizExcelImportRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses Excel file for Quiz import.
 * 
 * The parser primarily maps by header name (case-insensitive,
 * spaces/underscores
 * ignored) and falls back to legacy fixed indexes when a header is missing.
 */
public class QuizExcelParser {

    private static final String H_PACKAGE_NAME = "packagename";
    private static final String H_PACKAGE_DESCRIPTION = "packagedescription";
    private static final String H_SUBJECT_NAME = "subjectname";
    private static final String H_SUBJECT_CODE = "subjectcode";
    private static final String H_SUBJECT_DESCRIPTION = "subjectdescription";
    private static final String H_SLOT_NAME = "slotname";
    private static final String H_SLOT_NUMBER = "slotnumber";
    private static final String H_SLOT_DESCRIPTION = "slotdescription";
    private static final String H_FOLDER_NAME = "foldername";
    private static final String H_FOLDER_DESCRIPTION = "folderdescription";
    private static final String H_STUDY_SET_NAME = "studysetname";
    private static final String H_STUDY_SET_DESCRIPTION = "studysetdescription";
    private static final String H_QUIZ_TITLE = "quiztitle";
    private static final String H_QUIZ_DESCRIPTION = "quizdescription";
    private static final String H_QUIZ_DIFFICULTY = "quizdifficulty";
    private static final String H_TIME_LIMIT_SECONDS = "timelimitseconds";
    private static final String H_PASSING_SCORE = "passingscore";
    private static final String H_QUESTION_TYPE = "questiontype";
    private static final String H_QUESTION_TEXT = "questiontext";
    private static final String H_EXPLANATION = "explanation";
    private static final String H_POINTS = "points";
    private static final String H_QUESTION_DIFFICULTY = "questiondifficulty";
    private static final String H_DATA_1 = "data1";
    private static final String H_DATA_2 = "data2";
    private static final String H_DATA_3 = "data3";
    private static final String H_DATA_4 = "data4";
    private static final String H_DATA_5 = "data5";

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

            int firstRowNum = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(firstRowNum);
            Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headerRow);
            QuizExcelHeaderValidator.validateRequiredHeaders(headerIndexMap, Arrays.asList(
                    H_PACKAGE_NAME,
                    H_PACKAGE_DESCRIPTION,
                    H_SUBJECT_NAME,
                    H_SUBJECT_CODE,
                    H_SUBJECT_DESCRIPTION,
                    H_SLOT_NAME,
                    H_SLOT_NUMBER,
                    H_SLOT_DESCRIPTION,
                    H_FOLDER_NAME,
                    H_FOLDER_DESCRIPTION,
                    H_STUDY_SET_NAME,
                    H_QUIZ_TITLE,
                    H_QUIZ_DESCRIPTION,
                    H_QUIZ_DIFFICULTY,
                    H_TIME_LIMIT_SECONDS,
                    H_PASSING_SCORE,
                    H_QUESTION_TYPE,
                    H_QUESTION_TEXT,
                    H_EXPLANATION,
                    H_POINTS,
                    H_QUESTION_DIFFICULTY,
                    H_DATA_1,
                    H_DATA_2,
                    H_DATA_3,
                    H_DATA_4,
                    H_DATA_5));
            boolean hasStudySetDescriptionColumn = headerIndexMap.containsKey(H_STUDY_SET_DESCRIPTION);

            for (int rowIndex = firstRowNum + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                QuizExcelImportRow importRow = parseRow(row, rowIndex + 1, headerIndexMap,
                        hasStudySetDescriptionColumn);
                rows.add(importRow);
            }
        }

        return rows;
    }

    private static QuizExcelImportRow parseRow(
            Row row,
            int rowNum,
            Map<String, Integer> headerIndexMap,
            boolean hasStudySetDescriptionColumn) {

        int quizStartIndex = hasStudySetDescriptionColumn ? 12 : 11;
        int questionStartIndex = hasStudySetDescriptionColumn ? 17 : 16;
        int dataStartIndex = hasStudySetDescriptionColumn ? 22 : 21;

        return QuizExcelImportRow.builder()
                .rowNumber(rowNum)
                // Package (A-B)
                .packageName(getValue(row, headerIndexMap, H_PACKAGE_NAME, 0))
                .packageDescription(getValue(row, headerIndexMap, H_PACKAGE_DESCRIPTION, 1))
                // Subject (C-E)
                .subjectName(getValue(row, headerIndexMap, H_SUBJECT_NAME, 2))
                .subjectCode(getValue(row, headerIndexMap, H_SUBJECT_CODE, 3))
                .subjectDescription(getValue(row, headerIndexMap, H_SUBJECT_DESCRIPTION, 4))
                // Slot (F-H)
                .slotName(getValue(row, headerIndexMap, H_SLOT_NAME, 5))
                .slotNumber(getValue(row, headerIndexMap, H_SLOT_NUMBER, 6))
                .slotDescription(getValue(row, headerIndexMap, H_SLOT_DESCRIPTION, 7))
                // Folder (I-J)
                .folderName(getValue(row, headerIndexMap, H_FOLDER_NAME, 8))
                .folderDescription(getValue(row, headerIndexMap, H_FOLDER_DESCRIPTION, 9))
                // StudySet (K-L)
                .studySetName(getValue(row, headerIndexMap, H_STUDY_SET_NAME, 10))
                .studySetDescription(hasStudySetDescriptionColumn
                        ? getValue(row, headerIndexMap, H_STUDY_SET_DESCRIPTION, 11)
                        : null)
                // Quiz
                .quizTitle(getValue(row, headerIndexMap, H_QUIZ_TITLE, quizStartIndex))
                .quizDescription(getValue(row, headerIndexMap, H_QUIZ_DESCRIPTION, quizStartIndex + 1))
                .quizDifficulty(getValue(row, headerIndexMap, H_QUIZ_DIFFICULTY, quizStartIndex + 2))
                .timeLimitSeconds(getValue(row, headerIndexMap, H_TIME_LIMIT_SECONDS, quizStartIndex + 3))
                .passingScore(getValue(row, headerIndexMap, H_PASSING_SCORE, quizStartIndex + 4))
                // Question
                .questionType(getValue(row, headerIndexMap, H_QUESTION_TYPE, questionStartIndex))
                .questionText(getValue(row, headerIndexMap, H_QUESTION_TEXT, questionStartIndex + 1))
                .explanation(getValue(row, headerIndexMap, H_EXPLANATION, questionStartIndex + 2))
                .points(getValue(row, headerIndexMap, H_POINTS, questionStartIndex + 3))
                .questionDifficulty(getValue(row, headerIndexMap, H_QUESTION_DIFFICULTY, questionStartIndex + 4))
                // Data1-Data5
                .data1(getValue(row, headerIndexMap, H_DATA_1, dataStartIndex))
                .data2(getValue(row, headerIndexMap, H_DATA_2, dataStartIndex + 1))
                .data3(getValue(row, headerIndexMap, H_DATA_3, dataStartIndex + 2))
                .data4(getValue(row, headerIndexMap, H_DATA_4, dataStartIndex + 3))
                .data5(getValue(row, headerIndexMap, H_DATA_5, dataStartIndex + 4))
                .build();
    }

    private static Map<String, Integer> buildHeaderIndexMap(Row headerRow) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        if (headerRow == null) {
            return headerIndexMap;
        }

        short firstCellNum = headerRow.getFirstCellNum();
        short lastCellNum = headerRow.getLastCellNum();

        if (firstCellNum < 0 || lastCellNum < 0) {
            return headerIndexMap;
        }

        for (int cellIndex = firstCellNum; cellIndex < lastCellNum; cellIndex++) {
            String value = getCellValue(headerRow, cellIndex);
            if (value == null) {
                continue;
            }

            String normalized = normalizeHeader(value);
            if (!normalized.isEmpty()) {
                headerIndexMap.putIfAbsent(normalized, cellIndex);
                // Backward-compatible alias: StudySetDesc
                if ("studysetdesc".equals(normalized)) {
                    headerIndexMap.putIfAbsent(H_STUDY_SET_DESCRIPTION, cellIndex);
                }
            }
        }

        return headerIndexMap;
    }

    private static String getValue(Row row, Map<String, Integer> headerIndexMap, String headerKey, int fallbackIndex) {
        Integer index = headerIndexMap.get(headerKey);
        return getCellValue(row, index != null ? index : fallbackIndex);
    }

    private static String normalizeHeader(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
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
