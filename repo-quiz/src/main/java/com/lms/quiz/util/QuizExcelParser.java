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
 * Hierarchy: Package -&gt; Folder -&gt; StudySet -&gt; Quiz -&gt; Questions
 *
 * The parser maps by header name (case-insensitive, spaces/underscores stripped).
 * Required headers: PackageName, PackageDescription, FolderName, FolderDescription,
 * StudySetName, QuizTitle, QuizDescription, QuizDifficulty, TimeLimitSeconds,
 * PassingScore, QuestionType, QuestionText, Explanation, Points, QuestionDifficulty,
 * Data1, Data2, Data3, Data4, Data5
 */
public class QuizExcelParser {

    private static final String H_PACKAGE_NAME        = "packagename";
    private static final String H_PACKAGE_DESCRIPTION = "packagedescription";
    private static final String H_FOLDER_NAME         = "foldername";
    private static final String H_FOLDER_DESCRIPTION  = "folderdescription";
    private static final String H_STUDY_SET_NAME      = "studysetname";
    private static final String H_STUDY_SET_DESC      = "studysetdescription";
    private static final String H_QUIZ_TITLE          = "quiztitle";
    private static final String H_QUIZ_DESCRIPTION    = "quizdescription";
    private static final String H_QUIZ_DIFFICULTY     = "quizdifficulty";
    private static final String H_TIME_LIMIT_SECONDS  = "timelimitseconds";
    private static final String H_PASSING_SCORE       = "passingscore";
    private static final String H_QUESTION_TYPE       = "questiontype";
    private static final String H_QUESTION_TEXT       = "questiontext";
    private static final String H_EXPLANATION         = "explanation";
    private static final String H_POINTS              = "points";
    private static final String H_QUESTION_DIFFICULTY = "questiondifficulty";
    private static final String H_DATA_1              = "data1";
    private static final String H_DATA_2              = "data2";
    private static final String H_DATA_3              = "data3";
    private static final String H_DATA_4              = "data4";
    private static final String H_DATA_5              = "data5";

    private QuizExcelParser() {
    }

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

            for (int rowIndex = firstRowNum + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                rows.add(parseRow(row, rowIndex + 1, headerIndexMap));
            }
        }

        return rows;
    }

    private static QuizExcelImportRow parseRow(Row row, int rowNum, Map<String, Integer> headerIndexMap) {
        return QuizExcelImportRow.builder()
                .rowNumber(rowNum)
                .packageName(getValue(row, headerIndexMap, H_PACKAGE_NAME))
                .packageDescription(getValue(row, headerIndexMap, H_PACKAGE_DESCRIPTION))
                .folderName(getValue(row, headerIndexMap, H_FOLDER_NAME))
                .folderDescription(getValue(row, headerIndexMap, H_FOLDER_DESCRIPTION))
                .studySetName(getValue(row, headerIndexMap, H_STUDY_SET_NAME))
                .studySetDescription(getValue(row, headerIndexMap, H_STUDY_SET_DESC))
                .quizTitle(getValue(row, headerIndexMap, H_QUIZ_TITLE))
                .quizDescription(getValue(row, headerIndexMap, H_QUIZ_DESCRIPTION))
                .quizDifficulty(getValue(row, headerIndexMap, H_QUIZ_DIFFICULTY))
                .timeLimitSeconds(getValue(row, headerIndexMap, H_TIME_LIMIT_SECONDS))
                .passingScore(getValue(row, headerIndexMap, H_PASSING_SCORE))
                .questionType(getValue(row, headerIndexMap, H_QUESTION_TYPE))
                .questionText(getValue(row, headerIndexMap, H_QUESTION_TEXT))
                .explanation(getValue(row, headerIndexMap, H_EXPLANATION))
                .points(getValue(row, headerIndexMap, H_POINTS))
                .questionDifficulty(getValue(row, headerIndexMap, H_QUESTION_DIFFICULTY))
                .data1(getValue(row, headerIndexMap, H_DATA_1))
                .data2(getValue(row, headerIndexMap, H_DATA_2))
                .data3(getValue(row, headerIndexMap, H_DATA_3))
                .data4(getValue(row, headerIndexMap, H_DATA_4))
                .data5(getValue(row, headerIndexMap, H_DATA_5))
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
                // Backward-compatible alias
                if ("studysetdesc".equals(normalized)) {
                    headerIndexMap.putIfAbsent(H_STUDY_SET_DESC, cellIndex);
                }
            }
        }

        return headerIndexMap;
    }

    private static String getValue(Row row, Map<String, Integer> headerIndexMap, String headerKey) {
        Integer index = headerIndexMap.get(headerKey);
        if (index == null) {
            return null;
        }
        return getCellValue(row, index);
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
