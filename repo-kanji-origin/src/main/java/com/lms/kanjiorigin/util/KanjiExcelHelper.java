package com.lms.kanjiorigin.util;

import com.lms.kanjiorigin.dto.request.ImportKanjiRequest;
import com.lms.kanjiorigin.dto.request.ImportLessonRequest;
import com.lms.kanjiorigin.dto.request.ImportQuestionRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KanjiExcelHelper {
    
    private static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    
    // Kanji columns: A(0)-K(10)
    private static final int KANJI_TERM_COL = 1;        // B: Hán tự
    private static final int KANJI_PINYIN_COL = 2;      // C: Pinyin
    private static final int KANJI_SINO_VN_COL = 3;     // D: Âm Hán Việt
    private static final int KANJI_MEANING_COL = 4;     // E: Nghĩa
    private static final int KANJI_ORIGIN_VI_COL = 5;   // F: Nguồn gốc (VI)
    private static final int KANJI_ORIGIN_CN_COL = 6;   // G: Nguồn gốc (CN)
    private static final int KANJI_IMAGE_COL = 7;       // H: Hình ảnh
    private static final int KANJI_EXAM_SENT_COL = 8;   // I: Câu ví dụ
    private static final int KANJI_EXAM_MEAN_COL = 9;   // J: Nghĩa câu VD
    private static final int KANJI_EXAM_PINY_COL = 10;  // K: Pinyin câu VD
    
    // Question columns: M(12)-P(15)
    private static final int QUESTION_CONTENT_COL = 13;       // N: Câu hỏi
    private static final int QUESTION_CORRECT_COL = 14;       // O: Đáp án đúng
    private static final int QUESTION_WRONG_COL = 15;         // P: Đáp án sai
    
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
        
        // Row 1: headers, skip
        // Row 2+: data rows
        for (int rowIdx = 2; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) continue;
            
            // Parse kanji from columns A-K
            ImportKanjiRequest kanji = parseKanjiFromRow(row);
            if (kanji != null && kanji.getTerm() != null && !kanji.getTerm().trim().isEmpty()) {
                lesson.getKanjis().add(kanji);
            }
            
            // Parse question from columns M-P
            ImportQuestionRequest question = parseQuestionFromRow(row);
            if (question != null && question.getContent() != null && !question.getContent().trim().isEmpty()) {
                lesson.getQuestions().add(question);
            }
        }
        
        return lesson;
    }
    
    private static ImportKanjiRequest parseKanjiFromRow(Row row) {
        String term = getCellValueAsString(row.getCell(KANJI_TERM_COL));
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        
        return ImportKanjiRequest.builder()
                .term(term.trim())
                .pinyin(getCellValueAsString(row.getCell(KANJI_PINYIN_COL)))
                .sinoVn(getCellValueAsString(row.getCell(KANJI_SINO_VN_COL)))
                .meaning(getCellValueAsString(row.getCell(KANJI_MEANING_COL)))
                .originTextVi(getCellValueAsString(row.getCell(KANJI_ORIGIN_VI_COL)))
                .originTextCn(getCellValueAsString(row.getCell(KANJI_ORIGIN_CN_COL)))
                .originImage(getCellValueAsString(row.getCell(KANJI_IMAGE_COL)))
                .exampleSentence(getCellValueAsString(row.getCell(KANJI_EXAM_SENT_COL)))
                .exampleMeaning(getCellValueAsString(row.getCell(KANJI_EXAM_MEAN_COL)))
                .examplePinyin(getCellValueAsString(row.getCell(KANJI_EXAM_PINY_COL)))
                .build();
    }
    
    private static ImportQuestionRequest parseQuestionFromRow(Row row) {
        String content = getCellValueAsString(row.getCell(QUESTION_CONTENT_COL));
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        String correctAnswer = getCellValueAsString(row.getCell(QUESTION_CORRECT_COL));
        String wrongOptionsStr = getCellValueAsString(row.getCell(QUESTION_WRONG_COL));
        
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
