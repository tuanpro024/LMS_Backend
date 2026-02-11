package com.lms.multimedia.util;

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parser for Karaoke Excel files
 * Validates and converts Excel data to Karaoke JSON format
 */
public class KaraokeExcelParser {

    private static final List<String> REQUIRED_COLUMNS = Arrays.asList(
            "Segment ID",
            "Start Time (s)",
            "End Time (s)",
            "Vietnamese Translation",
            "Word",
            "Pinyin",
            "Word Start (s)",
            "Word End (s)");

    @Data
    public static class ParseResult {
        private boolean success;
        private List<KaraokeSegment> data;
        private List<String> errors = new ArrayList<>();
        private Map<String, Integer> stats = new HashMap<>();
    }

    @Data
    public static class KaraokeSegment {
        private Integer id;
        private Double startTime;
        private Double endTime;
        private String vietnamese;
        private List<KaraokeWord> words = new ArrayList<>();
    }

    @Data
    public static class KaraokeWord {
        private String char_; // "char" is reserved keyword
        private String pinyin;
        private Double startTime;
        private Double endTime;
    }

    /**
     * Parse and validate Excel file
     */
    public static ParseResult parseKaraokeExcel(MultipartFile file) {
        ParseResult result = new ParseResult();
        result.setSuccess(false);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Read rows
            List<Map<String, String>> rows = readRows(sheet);

            if (rows.isEmpty()) {
                result.getErrors().add("Excel file is empty. Please add data rows.");
                return result;
            }

            // Validate
            List<String> validationErrors = validateRows(rows);
            if (!validationErrors.isEmpty()) {
                result.setErrors(validationErrors);
                return result;
            }

            // Convert to Karaoke format
            List<KaraokeSegment> karaokeData = convertToKaraokeFormat(rows);

            // Build result
            result.setSuccess(true);
            result.setData(karaokeData);
            result.getStats().put("segments", karaokeData.size());
            result.getStats().put("words", karaokeData.stream()
                    .mapToInt(seg -> seg.getWords().size())
                    .sum());

            return result;

        } catch (IOException e) {
            result.getErrors().add("Failed to parse Excel file: " + e.getMessage());
            return result;
        }
    }

    /**
     * Read all rows from sheet into Map format
     */
    private static List<Map<String, String>> readRows(Sheet sheet) {
        List<Map<String, String>> rows = new ArrayList<>();

        if (sheet.getPhysicalNumberOfRows() < 2) {
            return rows; // No data rows
        }

        // Read header row
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValue(cell));
        }

        // Read data rows
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            Map<String, String> rowData = new HashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                rowData.put(headers.get(j), cell != null ? getCellValue(cell) : "");
            }
            rows.add(rowData);
        }

        return rows;
    }

    /**
     * Get cell value as string
     */
    private static String getCellValue(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Validate all rows
     */
    private static List<String> validateRows(List<Map<String, String>> rows) {
        List<String> errors = new ArrayList<>();

        // Check required columns
        Map<String, String> firstRow = rows.get(0);
        List<String> missingCols = REQUIRED_COLUMNS.stream()
                .filter(col -> !firstRow.containsKey(col))
                .collect(Collectors.toList());

        if (!missingCols.isEmpty()) {
            errors.add("Missing required columns: " + String.join(", ", missingCols));
            return errors; // Stop validation if columns missing
        }

        // Validate each row
        for (int idx = 0; idx < rows.size(); idx++) {
            Map<String, String> row = rows.get(idx);
            int rowNum = idx + 2; // Excel row number (1-indexed + header)

            // Check for missing values
            for (String col : REQUIRED_COLUMNS) {
                String value = row.get(col);
                if (value == null || value.trim().isEmpty()) {
                    errors.add(String.format("Row %d: Missing value for \"%s\"", rowNum, col));
                }
            }

            // Validate time ranges
            try {
                double segStart = Double.parseDouble(row.get("Start Time (s)"));
                double segEnd = Double.parseDouble(row.get("End Time (s)"));
                double wordStart = Double.parseDouble(row.get("Word Start (s)"));
                double wordEnd = Double.parseDouble(row.get("Word End (s)"));

                if (segStart >= segEnd) {
                    errors.add(String.format("Row %d: Segment start time must be less than end time", rowNum));
                }

                if (wordStart < segStart) {
                    errors.add(String.format("Row %d: Word start time (%.2fs) is before segment start (%.2fs)",
                            rowNum, wordStart, segStart));
                }

                if (wordEnd > segEnd) {
                    errors.add(String.format("Row %d: Word end time (%.2fs) is after segment end (%.2fs)",
                            rowNum, wordEnd, segEnd));
                }

                if (wordStart >= wordEnd) {
                    errors.add(String.format("Row %d: Word start time must be less than word end time", rowNum));
                }

            } catch (NumberFormatException e) {
                errors.add(String.format("Row %d: Time values must be numbers", rowNum));
            }
        }

        // Check segment consistency
        Map<String, List<SegmentInfo>> segmentGroups = new HashMap<>();
        for (int idx = 0; idx < rows.size(); idx++) {
            Map<String, String> row = rows.get(idx);
            String segId = row.get("Segment ID");

            segmentGroups.computeIfAbsent(segId, k -> new ArrayList<>())
                    .add(new SegmentInfo(
                            idx + 2,
                            row.get("Start Time (s)"),
                            row.get("End Time (s)"),
                            row.get("Vietnamese Translation")));
        }

        segmentGroups.forEach((segId, infos) -> {
            Set<String> startTimes = infos.stream().map(i -> i.start).collect(Collectors.toSet());
            Set<String> endTimes = infos.stream().map(i -> i.end).collect(Collectors.toSet());
            Set<String> translations = infos.stream().map(i -> i.vietnamese).collect(Collectors.toSet());

            if (startTimes.size() > 1) {
                errors.add(String.format(
                        "Segment %s: Inconsistent start times. All rows with same Segment ID must have same start time.",
                        segId));
            }
            if (endTimes.size() > 1) {
                errors.add(String.format(
                        "Segment %s: Inconsistent end times. All rows with same Segment ID must have same end time.",
                        segId));
            }
            if (translations.size() > 1) {
                errors.add(String.format(
                        "Segment %s: Inconsistent Vietnamese translations. All rows with same Segment ID must have same translation.",
                        segId));
            }
        });

        return errors;
    }

    /**
     * Convert rows to Karaoke format
     */
    private static List<KaraokeSegment> convertToKaraokeFormat(List<Map<String, String>> rows) {
        Map<Integer, KaraokeSegment> segmentGroups = new LinkedHashMap<>();

        for (Map<String, String> row : rows) {
            int segId = Integer.parseInt(row.get("Segment ID"));

            KaraokeSegment segment = segmentGroups.computeIfAbsent(segId, k -> {
                KaraokeSegment seg = new KaraokeSegment();
                seg.setId(segId);
                seg.setStartTime(Double.parseDouble(row.get("Start Time (s)")));
                seg.setEndTime(Double.parseDouble(row.get("End Time (s)")));
                seg.setVietnamese(row.get("Vietnamese Translation"));
                return seg;
            });

            // Add word to segment
            KaraokeWord word = new KaraokeWord();
            word.setChar_(row.get("Word"));
            word.setPinyin(row.get("Pinyin"));
            word.setStartTime(Double.parseDouble(row.get("Word Start (s)")));
            word.setEndTime(Double.parseDouble(row.get("Word End (s)")));
            segment.getWords().add(word);
        }

        // Return sorted by segment ID
        return segmentGroups.values().stream()
                .sorted(Comparator.comparing(KaraokeSegment::getId))
                .collect(Collectors.toList());
    }

    /**
     * Helper class for segment validation
     */
    @Data
    private static class SegmentInfo {
        private final int rowNum;
        private final String start;
        private final String end;
        private final String vietnamese;
    }
}
