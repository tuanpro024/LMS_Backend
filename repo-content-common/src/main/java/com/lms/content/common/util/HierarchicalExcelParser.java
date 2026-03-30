package com.lms.content.common.util;

import com.lms.content.common.dto.excel.HierarchicalImportRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parser for hierarchical Excel structure.
 * Format: Package -&gt; Folder -&gt; StudySet -&gt; Content Items
 *
 * Parsing is header-name-based (case-insensitive, spaces/underscores stripped).
 * Required headers: PackageName, PackageDescription, FolderName,
 * FolderDescription,
 * StudySetName, StudySetDescription, Term, Definition, Pinyin, SinoVn,
 * WordType,
 * HskLevel, ImageWord, SinoOrigin, ImageOrigin, Audio, ExampleSentence,
 * ExamplePinyin,
 * ExampleMeaning, CharactersJson
 */
public class HierarchicalExcelParser {

    private static final String H_PACKAGE_NAME = "packagename";
    private static final String H_PACKAGE_DESCRIPTION = "packagedescription";
    private static final String H_FOLDER_NAME = "foldername";
    private static final String H_FOLDER_DESCRIPTION = "folderdescription";
    private static final String H_STUDY_SET_NAME = "studysetname";
    private static final String H_STUDY_SET_DESC = "studysetdescription";
    private static final String H_TERM = "term";
    private static final String H_DEFINITION = "definition";
    private static final String H_PINYIN = "pinyin";
    private static final String H_SINO_VN = "sinovn";
    private static final String H_WORD_TYPE = "wordtype";
    private static final String H_HSK_LEVEL = "hsklevel";
    private static final String H_IMAGE_WORD = "imageword";
    private static final String H_SINO_ORIGIN = "sinoorigin";
    private static final String H_IMAGE_ORIGIN = "imageorigin";
    private static final String H_AUDIO = "audio";
    private static final String H_EXAMPLE_SENTENCE = "examplesentence";
    private static final String H_EXAMPLE_PINYIN = "examplepinyin";
    private static final String H_EXAMPLE_MEANING = "examplemeaning";
    private static final String H_CHARACTERS_JSON = "charactersjson";

    private static final List<String> REQUIRED_HEADERS = Arrays.asList(
            H_PACKAGE_NAME, H_PACKAGE_DESCRIPTION,
            H_FOLDER_NAME, H_FOLDER_DESCRIPTION,
            H_STUDY_SET_NAME, H_STUDY_SET_DESC,
            H_TERM, H_DEFINITION, H_PINYIN, H_SINO_VN,
            H_WORD_TYPE, H_HSK_LEVEL, H_IMAGE_WORD,
            H_SINO_ORIGIN, H_IMAGE_ORIGIN, H_AUDIO,
            H_EXAMPLE_SENTENCE, H_EXAMPLE_PINYIN,
            H_EXAMPLE_MEANING, H_CHARACTERS_JSON);

    private HierarchicalExcelParser() {
    }

    /**
     * Parse Excel file into hierarchical rows.
     */
    public static List<HierarchicalImportRow> parseFile(MultipartFile file) throws IOException {
        return parseFile(file, REQUIRED_HEADERS);
    }

    /**
     * Parse Excel file into hierarchical rows with custom required headers.
     */
    public static List<HierarchicalImportRow> parseFile(MultipartFile file, List<String> requiredHeaders)
            throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is null");
        }
        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            throw new IllegalArgumentException("Invalid file format. Only .xlsx and .xls are supported");
        }

        try (InputStream is = file.getInputStream()) {
            return parseExcel(is, normalizeRequiredHeaders(requiredHeaders));
        }
    }

    private static List<HierarchicalImportRow> parseExcel(InputStream is, List<String> requiredHeaders)
            throws IOException {
        List<HierarchicalImportRow> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return rows;
            }

            int firstRowNum = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(firstRowNum);
            Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headerRow);
            validateRequiredHeaders(headerIndexMap, requiredHeaders);

            for (int i = firstRowNum + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                rows.add(parseRow(row, i + 1, headerIndexMap));
            }
        }

        return rows;
    }

    private static HierarchicalImportRow parseRow(Row row, int rowNumber, Map<String, Integer> headerIndexMap) {
        return HierarchicalImportRow.builder()
                .rowNumber(rowNumber)
                .packageName(getValue(row, headerIndexMap, H_PACKAGE_NAME))
                .packageDescription(getValue(row, headerIndexMap, H_PACKAGE_DESCRIPTION))
                .folderName(getValue(row, headerIndexMap, H_FOLDER_NAME))
                .folderDescription(getValue(row, headerIndexMap, H_FOLDER_DESCRIPTION))
                .studySetName(getValue(row, headerIndexMap, H_STUDY_SET_NAME))
                .studySetDescription(getValue(row, headerIndexMap, H_STUDY_SET_DESC))
                .term(getValue(row, headerIndexMap, H_TERM))
                .definition(getValue(row, headerIndexMap, H_DEFINITION))
                .pinyin(getValue(row, headerIndexMap, H_PINYIN))
                .sinoVn(getValue(row, headerIndexMap, H_SINO_VN))
                .wordType(getValue(row, headerIndexMap, H_WORD_TYPE))
                .hskLevel(getValue(row, headerIndexMap, H_HSK_LEVEL))
                .imageWord(getValue(row, headerIndexMap, H_IMAGE_WORD))
                .sinoOrigin(getValue(row, headerIndexMap, H_SINO_ORIGIN))
                .imageOrigin(getValue(row, headerIndexMap, H_IMAGE_ORIGIN))
                .audio(getValue(row, headerIndexMap, H_AUDIO))
                .exampleSentence(getValue(row, headerIndexMap, H_EXAMPLE_SENTENCE))
                .examplePinyin(getValue(row, headerIndexMap, H_EXAMPLE_PINYIN))
                .exampleMeaning(getValue(row, headerIndexMap, H_EXAMPLE_MEANING))
                .charactersJson(getValue(row, headerIndexMap, H_CHARACTERS_JSON))
                .build();
    }

    private static Map<String, Integer> buildHeaderIndexMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) {
            return map;
        }
        short firstCell = headerRow.getFirstCellNum();
        short lastCell = headerRow.getLastCellNum();
        if (firstCell < 0 || lastCell < 0) {
            return map;
        }
        for (int i = firstCell; i < lastCell; i++) {
            String value = getCellValue(headerRow, i);
            if (value != null) {
                String normalized = normalizeHeader(value);
                if (!normalized.isEmpty()) {
                    map.putIfAbsent(normalized, i);
                    if ("studetsetdescription".equals(normalized)) {
                        map.putIfAbsent(H_STUDY_SET_DESC, i);
                    }
                }
            }
        }
        return map;
    }

    private static void validateRequiredHeaders(Map<String, Integer> headerIndexMap, List<String> requiredHeaders) {
        List<String> missing = new ArrayList<>();
        for (String required : requiredHeaders) {
            if (!headerIndexMap.containsKey(required)) {
                missing.add(required);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required Excel headers: " + String.join(", ", missing));
        }
    }

    private static String getValue(Row row, Map<String, Integer> headerIndexMap, String headerKey) {
        Integer index = headerIndexMap.get(headerKey);
        if (index == null) {
            return null;
        }
        return getCellValue(row, index);
    }

    private static List<String> normalizeRequiredHeaders(List<String> requiredHeaders) {
        if (requiredHeaders == null || requiredHeaders.isEmpty()) {
            return REQUIRED_HEADERS;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String required : requiredHeaders) {
            if (required == null) {
                continue;
            }
            String key = normalizeHeader(required);
            if (!key.isEmpty()) {
                normalized.add(key);
            }
        }
        if (normalized.isEmpty()) {
            return REQUIRED_HEADERS;
        }
        return new ArrayList<>(normalized);
    }

    private static String normalizeHeader(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> {
                String value = cell.getStringCellValue();
                yield (value != null && !value.trim().isEmpty()) ? value.trim() : null;
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    yield String.valueOf((long) numValue);
                }
                yield String.valueOf(numValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    String v = cell.getStringCellValue();
                    yield (v != null && !v.trim().isEmpty()) ? v.trim() : null;
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
