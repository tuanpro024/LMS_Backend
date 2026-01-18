package com.lms.writing.util;

import com.lms.writing.exception.InvalidFileFormatException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelParser {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Parse file Excel hoặc CSV
     */
    public static List<ExcelRow> parseFile(MultipartFile file) {
        validateFile(file);

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new InvalidFileFormatException("File name is null");
        }

        try {
            if (filename.toLowerCase().endsWith(".xlsx")) {
                return parseXlsx(file.getInputStream());
            } else if (filename.toLowerCase().endsWith(".csv")) {
                return parseCsv(file.getInputStream());
            } else {
                throw new InvalidFileFormatException("Unsupported file format. Only .xlsx and .csv are allowed");
            }
        } catch (IOException e) {
            log.error("Error parsing file: {}", filename, e);
            throw new InvalidFileFormatException("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * Validate file before parsing
     */
    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileFormatException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileFormatException("File size exceeds maximum limit of 10MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null
                || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".csv"))) {
            throw new InvalidFileFormatException("Invalid file extension. Only .xlsx and .csv are supported");
        }
    }

    /**
     * Parse file XLSX
     */
    private static List<ExcelRow> parseXlsx(InputStream inputStream) throws IOException {
        List<ExcelRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            int rowNumber = 0;
            for (Row row : sheet) {
                rowNumber++;

                // Skip header row (row 0)
                if (rowNumber == 1) {
                    continue;
                }

                ExcelRow excelRow = ExcelRow.builder()
                        .rowNumber(rowNumber)
                        .studySetName(getCellValue(row.getCell(0))) // Bài học
                        .term(getCellValue(row.getCell(1))) // Term
                        .pinyin(getCellValue(row.getCell(2))) // pinyin
                        .sinoVn(getCellValue(row.getCell(3))) // sinoVn
                        .definition(getCellValue(row.getCell(4))) // definition
                        .wordType(getCellValue(row.getCell(5))) // wordType
                        .hskLevel(getCellValue(row.getCell(6))) // hskLevel
                        .imageWord(getCellValue(row.getCell(7))) // imageWord
                        .sinoOrigin(getCellValue(row.getCell(8))) // sinoOrigin
                        .imageOrigin(getCellValue(row.getCell(9))) // ImageOrigin
                        .exampleSentence(getCellValue(row.getCell(10))) // exampleSentences
                        .examplePinyin(getCellValue(row.getCell(11))) // examplePinyin
                        .exampleMeaning(getCellValue(row.getCell(12))) // exampleMeaning
                        .charactersJson(getCellValue(row.getCell(13))) // Characters
                        .build();

                rows.add(excelRow);
            }
        }

        return rows;
    }

    /**
     * Parse file CSV
     */
    private static List<ExcelRow> parseCsv(InputStream inputStream) throws IOException {
        List<ExcelRow> rows = new ArrayList<>();

        try (CSVParser csvParser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            int rowNumber = 1;
            for (CSVRecord record : csvParser) {
                rowNumber++;

                ExcelRow excelRow = ExcelRow.builder()
                        .rowNumber(rowNumber)
                        .studySetName(getRecordValue(record, 0)) // Bài học
                        .term(getRecordValue(record, 1)) // Term
                        .pinyin(getRecordValue(record, 2)) // pinyin
                        .sinoVn(getRecordValue(record, 3)) // sinoVn
                        .definition(getRecordValue(record, 4)) // definition
                        .wordType(getRecordValue(record, 5)) // wordType
                        .hskLevel(getRecordValue(record, 6)) // hskLevel
                        .imageWord(getRecordValue(record, 7)) // imageWord
                        .sinoOrigin(getRecordValue(record, 8)) // sinoOrigin
                        .imageOrigin(getRecordValue(record, 9)) // ImageOrigin
                        .exampleSentence(getRecordValue(record, 10)) // exampleSentences
                        .examplePinyin(getRecordValue(record, 11)) // examplePinyin
                        .exampleMeaning(getRecordValue(record, 12)) // exampleMeaning
                        .charactersJson(getRecordValue(record, 13)) // Characters
                        .build();

                rows.add(excelRow);
            }
        }

        return rows;
    }

    /**
     * Lấy giá trị cell an toàn
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        yield String.valueOf((long) numericValue);
                    } else {
                        yield String.valueOf(numericValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * Lấy giá trị từ CSV record an toàn
     */
    private static String getRecordValue(CSVRecord record, int index) {
        try {
            if (index >= record.size()) {
                return null;
            }
            String value = record.get(index);
            return (value == null || value.trim().isEmpty()) ? null : value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kiểm tra row có trống không
     */
    public static boolean isRowEmpty(ExcelRow row) {
        return (row.getStudySetName() == null || row.getStudySetName().isEmpty())
                && (row.getTerm() == null || row.getTerm().isEmpty())
                && (row.getDefinition() == null || row.getDefinition().isEmpty())
                && (row.getPinyin() == null || row.getPinyin().isEmpty())
                && (row.getSinoVn() == null || row.getSinoVn().isEmpty())
                && (row.getWordType() == null || row.getWordType().isEmpty())
                && (row.getHskLevel() == null || row.getHskLevel().isEmpty())
                && (row.getImageWord() == null || row.getImageWord().isEmpty())
                && (row.getSinoOrigin() == null || row.getSinoOrigin().isEmpty())
                && (row.getImageOrigin() == null || row.getImageOrigin().isEmpty())
                && (row.getExampleSentence() == null || row.getExampleSentence().isEmpty())
                && (row.getExamplePinyin() == null || row.getExamplePinyin().isEmpty())
                && (row.getExampleMeaning() == null || row.getExampleMeaning().isEmpty())
                && (row.getCharactersJson() == null || row.getCharactersJson().isEmpty());
    }

    /**
     * Data class đại diện cho một dòng trong Excel
     * Mapping: Bài học(0), Term(1), pinyin(2), sinoVn(3), definition(4),
     * wordType(5), hskLevel(6), imageWord(7), sinoOrigin(8), ImageOrigin(9),
     * exampleSentences(10), examplePinyin(11), exampleMeaning(12), Characters(13)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExcelRow {
        private int rowNumber;
        private String studySetName; // Column 0 - Bài học
        private String term; // Column 1 - Term (Hán tự)
        private String pinyin; // Column 2 - Pinyin
        private String sinoVn; // Column 3 - Âm Hán Việt
        private String definition; // Column 4 - Nghĩa tiếng Việt
        private String wordType; // Column 5 - Loại từ
        private String hskLevel; // Column 6 - Cấp độ HSK
        private String imageWord; // Column 7 - Từ khóa gợi nhớ
        private String sinoOrigin; // Column 8 - Nguồn gốc từ
        private String imageOrigin; // Column 9 - Tên file ảnh
        private String exampleSentence; // Column 10 - Câu ví dụ
        private String examplePinyin; // Column 11 - Pinyin câu ví dụ
        private String exampleMeaning; // Column 12 - Nghĩa câu ví dụ
        private String charactersJson; // Column 13 - JSON cấu trúc bộ thủ
    }
}
