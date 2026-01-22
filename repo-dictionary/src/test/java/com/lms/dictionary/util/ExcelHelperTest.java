package com.lms.dictionary.util;

import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelHelperTest {

    @Test
    public void testExcelToVocabularies() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vocabularies");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Hanzi");
        header.createCell(1).setCellValue("Pinyin");
        header.createCell(2).setCellValue("Meaning");
        header.createCell(3).setCellValue("Example");
        header.createCell(4).setCellValue("Word Type");

        // Data Row 1
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("你好");
        row1.createCell(1).setCellValue("nǐ hǎo");
        row1.createCell(2).setCellValue("Hello; Hi");
        row1.createCell(3).setCellValue("你好吗？");
        row1.createCell(4).setCellValue("Phrase");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            new ByteArrayInputStream(bos.toByteArray()));

        Assertions.assertTrue(ExcelHelper.hasExcelFormat(file));

        List<CreateVocabularyRequest> requests = ExcelHelper.excelToVocabularies(file.getInputStream());

        Assertions.assertEquals(1, requests.size());
        Assertions.assertEquals("你好", requests.get(0).getHanzi());
        Assertions.assertEquals("nǐ hǎo", requests.get(0).getPinyin());
        Assertions.assertEquals(2, requests.get(0).getMeanings().size()); // Hello; Hi
        Assertions.assertEquals("Hello", requests.get(0).getMeanings().get(0).getMeaning());
        Assertions.assertEquals("你好吗？", requests.get(0).getMeanings().get(0).getExampleSentenceCn());
    }
}
