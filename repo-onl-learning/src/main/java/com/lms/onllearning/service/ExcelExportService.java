package com.lms.onllearning.service;

import com.lms.onllearning.entity.LeadRegistration;
import com.lms.onllearning.repository.LeadRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Excel export sử dụng SXSSFWorkbook (streaming) để tránh OOM với dữ liệu lớn.
 * Flush mỗi 100 rows xuống disk.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int ROW_ACCESS_WINDOW = 100;

    private final LeadRegistrationRepository repository;

    @Transactional(readOnly = true)
    public byte[] exportLeads(String courseCode, LocalDate from, LocalDate to) throws IOException {

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW);
                Stream<LeadRegistration> stream = repository.streamForExport(courseCode, fromDt, toDt);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Danh sách đăng ký");

            // Header row
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = { "STT", "Họ và tên", "Email", "Số điện thoại",
                    "Khóa học", "Loại khóa học", "Ngày đăng ký", "Trạng thái", "Ghi chú" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            AtomicInteger rowNum = new AtomicInteger(1);
            stream.forEach(lead -> {
                Row row = sheet.createRow(rowNum.getAndIncrement());
                row.createCell(0).setCellValue(rowNum.get() - 1);
                row.createCell(1).setCellValue(lead.getFullName());
                row.createCell(2).setCellValue(lead.getEmail());
                row.createCell(3).setCellValue(lead.getPhone());
                row.createCell(4).setCellValue(lead.getCourseName());
                row.createCell(5).setCellValue(lead.getCourseType() != null ? lead.getCourseType() : "");
                row.createCell(6).setCellValue(
                        lead.getRegisteredAt() != null
                                ? lead.getRegisteredAt().format(DATE_FMT)
                                : "");
                row.createCell(7).setCellValue(lead.getStatus() != null ? lead.getStatus().name() : "");
                row.createCell(8).setCellValue(lead.getNote() != null ? lead.getNote() : "");
            });

            workbook.write(out);
            log.info("Excel exported: {} rows", rowNum.get() - 1);
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
}
