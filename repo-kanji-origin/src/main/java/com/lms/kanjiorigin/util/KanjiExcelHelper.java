package com.lms.kanjiorigin.util;

import com.lms.kanjiorigin.dto.request.ImportKanjiRequest;
import com.lms.kanjiorigin.dto.request.ImportLessonRequest;
import com.lms.kanjiorigin.dto.request.ImportQuestionRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KanjiExcelHelper {

    private static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final String H_TERM = "term";
    private static final String H_PINYIN = "pinyin";
    private static final String H_SINO_VN = "sinovn";
    private static final String H_MEANING = "meaning";
    private static final String H_ORIGIN_TEXT_VI = "origintextvi";
    private static final String H_ORIGIN_TEXT_CN = "origintextcn";
    private static final String H_ORIGIN_IMAGE = "originimage";
    private static final String H_EXAMPLE_SENTENCE = "examplesentence";
    private static final String H_EXAMPLE_MEANING = "examplemeaning";
    private static final String H_EXAMPLE_PINYIN = "examplepinyin";

    private static final String H_QUESTION_CONTENT = "questioncontent";
    private static final String H_CORRECT_ANSWER = "correctanswer";
    private static final String H_WRONG_OPTIONS = "wrongoptions";

    private static final Map<String, List<String>> HEADER_ALIASES = Map.ofEntries(
            Map.entry(H_TERM, List.of("term", "hantu", "kanji", "character")),
            Map.entry(H_PINYIN, List.of("pinyin")),
            Map.entry(H_SINO_VN, List.of("sinovn", "amhanviet", "hanviet")),
            Map.entry(H_MEANING, List.of("meaning", "nghia")),
            Map.entry(H_ORIGIN_TEXT_VI, List.of("origintextvi", "originvi", "nguongocvi", "nguongoc")),
            Map.entry(H_ORIGIN_TEXT_CN, List.of("origintextcn", "origincn", "nguongoccn")),
            Map.entry(H_ORIGIN_IMAGE, List.of("originimage", "image", "hinhanh", "imageword")),
            Map.entry(H_EXAMPLE_SENTENCE, List.of("examplesentence", "cauvidu", "vidu")),
            Map.entry(H_EXAMPLE_MEANING, List.of("examplemeaning", "nghiacauvidu", "nghiacauvd")),
            Map.entry(H_EXAMPLE_PINYIN, List.of("examplepinyin", "pinyincauvidu", "pinyincauvd")),
            Map.entry(H_QUESTION_CONTENT, List.of("questioncontent", "question", "cauhoi")),
            Map.entry(H_CORRECT_ANSWER, List.of("correctanswer", "dapandung", "answercorrect")),
            Map.entry(H_WRONG_OPTIONS, List.of("wrongoptions", "wrongoption", "wronganswer", "dapansai")));

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static List<ImportLessonRequest> excelToLessons(InputStream is) throws IOException {
        List<ImportLessonRequest> lessons = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                ImportLessonRequest lesson = parseSheet(sheet);
                if (lesson != null) {
                    lessons.add(lesson);
                }
            }
        }

        return lessons;
    }

    private static ImportLessonRequest parseSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();
        if (sheetName == null || sheetName.trim().isEmpty()) {
            return null;
        }

        ImportLessonRequest lesson = new ImportLessonRequest();
        lesson.setTitle(sheetName.trim());
        lesson.setKanjis(new ArrayList<>());
        lesson.setQuestions(new ArrayList<>());

        // Row 0: description in cell A1
        Row descRow = sheet.getRow(0);
        if (descRow != null) {
            Cell descCell = descRow.getCell(0);
            if (descCell != null) {
                lesson.setDescription(getCellValueAsString(descCell));
            }
        }

        Row headerRow = sheet.getRow(1);
        Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headerRow);
        validateRequiredHeaders(headerIndexMap);

        // Row 1: headers, skip
        // Row 2+: data rows
        for (int rowIdx = 2; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null)
                continue;

            // Parse kanji from header names
            ImportKanjiRequest kanji = parseKanjiFromRow(row, headerIndexMap);
            if (kanji != null && kanji.getTerm() != null && !kanji.getTerm().trim().isEmpty()) {
                lesson.getKanjis().add(kanji);
            }

            // Parse question from header names
            ImportQuestionRequest question = parseQuestionFromRow(row, headerIndexMap);
            if (question != null && question.getContent() != null && !question.getContent().trim().isEmpty()) {
                lesson.getQuestions().add(question);
            }
        }

        return lesson;
    }

    private static ImportKanjiRequest parseKanjiFromRow(Row row, Map<String, Integer> headerIndexMap) {
        String term = getValue(row, headerIndexMap, H_TERM);
        if (term == null || term.trim().isEmpty()) {
            return null;
        }

        return ImportKanjiRequest.builder()
                .term(term.trim())
                .pinyin(getValue(row, headerIndexMap, H_PINYIN))
                .sinoVn(getValue(row, headerIndexMap, H_SINO_VN))
                .meaning(getValue(row, headerIndexMap, H_MEANING))
                .originTextVi(getValue(row, headerIndexMap, H_ORIGIN_TEXT_VI))
                .originTextCn(getValue(row, headerIndexMap, H_ORIGIN_TEXT_CN))
                .originImage(getValue(row, headerIndexMap, H_ORIGIN_IMAGE))
                .exampleSentence(getValue(row, headerIndexMap, H_EXAMPLE_SENTENCE))
                .exampleMeaning(getValue(row, headerIndexMap, H_EXAMPLE_MEANING))
                .examplePinyin(getValue(row, headerIndexMap, H_EXAMPLE_PINYIN))
                .build();
    }

    private static ImportQuestionRequest parseQuestionFromRow(Row row, Map<String, Integer> headerIndexMap) {
        String content = getValue(row, headerIndexMap, H_QUESTION_CONTENT);
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String correctAnswer = getValue(row, headerIndexMap, H_CORRECT_ANSWER);
        String wrongOptionsStr = getValue(row, headerIndexMap, H_WRONG_OPTIONS);

        List<String> wrongOptions = new ArrayList<>();
        if (wrongOptionsStr != null && !wrongOptionsStr.trim().isEmpty()) {
            wrongOptions = Arrays.stream(wrongOptionsStr.split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return ImportQuestionRequest.builder()
                .content(content.trim())
                .correctAnswer(correctAnswer != null ? correctAnswer.trim() : "")
                .wrongOptions(wrongOptions)
                .build();
    }

    private static Map<String, Integer> buildHeaderIndexMap(Row headerRow) {
        Map<String, Integer> rawHeaders = new HashMap<>();
        if (headerRow == null) {
            return rawHeaders;
        }

        short firstCell = headerRow.getFirstCellNum();
        short lastCell = headerRow.getLastCellNum();
        if (firstCell < 0 || lastCell < 0) {
            return rawHeaders;
        }

        for (int i = firstCell; i < lastCell; i++) {
            String value = getCellValueAsString(headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            String normalized = normalizeHeader(value);
            if (!normalized.isEmpty()) {
                rawHeaders.putIfAbsent(normalized, i);
            }
        }

        Map<String, Integer> canonical = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : HEADER_ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                Integer index = rawHeaders.get(normalizeHeader(alias));
                if (index != null) {
                    canonical.putIfAbsent(entry.getKey(), index);
                    break;
                }
            }
        }

        return canonical;
    }

    private static void validateRequiredHeaders(Map<String, Integer> headerIndexMap) {
        if (!headerIndexMap.containsKey(H_TERM)) {
            throw new IllegalArgumentException("Missing required header: term (or alias like Hán tự)");
        }
    }

    private static String normalizeHeader(String value) {
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return ascii.replaceAll("[^a-z0-9]", "");
    }

    private static String getValue(Row row, Map<String, Integer> headerIndexMap, String headerKey) {
        Integer index = headerIndexMap.get(headerKey);
        if (index == null) {
            return null;
        }
        return getCellValueAsString(row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        yield String.valueOf((long) numValue);
                    }
                    yield String.valueOf(numValue);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        yield String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }
}
