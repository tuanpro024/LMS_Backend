package com.lms.flashcard.util;

import com.lms.flashcard.exception.InvalidFileFormatException;
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
            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên

            int rowNumber = 0;
            for (Row row : sheet) {
                rowNumber++;

                // Skip header row (row 0)
                if (rowNumber == 1) {
                    continue;
                }

                ExcelRow excelRow = ExcelRow.builder()
                        .rowNumber(rowNumber)
                        .studySetName(getCellValue(row.getCell(0)))
                        .term(getCellValue(row.getCell(1)))
                        .definition(getCellValue(row.getCell(2)))
                        .imageFileName(getCellValue(row.getCell(3)))
                        .pinyin(getCellValue(row.getCell(4)))
                        .pronunciation(getCellValue(row.getCell(5)))
                        .exampleSentence(getCellValue(row.getCell(6)))
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

            int rowNumber = 1; // Bắt đầu từ 1 vì đã skip header
            for (CSVRecord record : csvParser) {
                rowNumber++;

                ExcelRow excelRow = ExcelRow.builder()
                        .rowNumber(rowNumber)
                        .studySetName(getRecordValue(record, 0))
                        .term(getRecordValue(record, 1))
                        .definition(getRecordValue(record, 2))
                        .imageFileName(getRecordValue(record, 3))
                        .pinyin(getRecordValue(record, 4))
                        .pronunciation(getRecordValue(record, 5))
                        .exampleSentence(getRecordValue(record, 6))
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
                    // Convert numeric to string without scientific notation
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
                && (row.getImageFileName() == null || row.getImageFileName().isEmpty())
                && (row.getPinyin() == null || row.getPinyin().isEmpty())
                && (row.getPronunciation() == null || row.getPronunciation().isEmpty())
                && (row.getExampleSentence() == null || row.getExampleSentence().isEmpty());
    }

    /**
     * Data class đại diện cho một dòng trong Excel
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExcelRow {
        private int rowNumber;
        private String studySetName;
        private String term;
        private String definition;
        private String imageFileName;
        private String pinyin; // Column E
        private String pronunciation; // Column F
        private String exampleSentence; // Column G
    }
}
