package com.lms.dictionary.util;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.dictionary.dto.request.CreateVocabularyRequest;
import com.lms.dictionary.dto.request.VocabComponentRequest;
import com.lms.dictionary.dto.request.VocabularyMeaningRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String SHEET = "Vocabularies";

    public static boolean hasExcelFormat(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            return false;
        }
        return true;
    }

    public static List<CreateVocabularyRequest> excelToVocabularies(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) {
                // Try getting the first sheet if "Vocabularies" doesn't exist
                sheet = workbook.getSheetAt(0);
            }

            Iterator<Row> rows = sheet.iterator();

            List<CreateVocabularyRequest> vocabularies = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                String hanzi = getCellValueAsString(currentRow.getCell(0));
                if (hanzi == null || hanzi.trim().isEmpty()) {
                    continue; // Skip empty rows
                }

                CreateVocabularyRequest vocabulary = new CreateVocabularyRequest();
                vocabulary.setHanzi(hanzi);
                vocabulary.setPinyin(getCellValueAsString(currentRow.getCell(1)));

                List<VocabularyMeaningRequest> meanings = new ArrayList<>();
                String meaningVi = getCellValueAsString(currentRow.getCell(2)); // Meaning(Vi)
                
                String exampleCn = getCellValueAsString(currentRow.getCell(3));
                String exampleVi = getCellValueAsString(currentRow.getCell(4));
                
                // HSK Level
                String hskStr = getCellValueAsString(currentRow.getCell(5));
                if (!hskStr.isEmpty()) {
                    try {
                        vocabulary.setHskLevel((int) Double.parseDouble(hskStr));
                    } catch (NumberFormatException e) {
                        // ignore or default
                    }
                }

                // IsSingle
                String isSingleStr = getCellValueAsString(currentRow.getCell(6));
                vocabulary.setIsSingleVocab(Boolean.parseBoolean(isSingleStr) || "TRUE".equalsIgnoreCase(isSingleStr));

                // Components: Hanzi:Pinyin;Hanzi:Pinyin
                String componentsStr = getCellValueAsString(currentRow.getCell(7));
                if (componentsStr != null && !componentsStr.isEmpty()) {
                    List<VocabComponentRequest> components = new ArrayList<>();
                    String[] comps = componentsStr.split(";");
                    int order = 1;
                    for (String comp : comps) {
                        String[] parts = comp.split(":");
                        String cHanzi = parts[0].trim();
                        String cPinyin = parts.length > 1 ? parts[1].trim() : "";
                        
                        // use newVocabulary to specify the component to link/create
                        // Since we don't assume ID is known.
                        CreateVocabularyRequest compReq = new CreateVocabularyRequest();
                        compReq.setHanzi(cHanzi);
                        compReq.setPinyin(cPinyin);
                        compReq.setIsSingleVocab(true); // Components are usually single

                        components.add(VocabComponentRequest.builder()
                                .newVocabulary(compReq) // Service will lookup by Hanzi/Pinyin match
                                .orderIndex(order++)
                                .build());
                    }
                    vocabulary.setComponents(components);
                }

                // URLs & Extra
                vocabulary.setAudioUrl(getCellValueAsString(currentRow.getCell(8)));
                vocabulary.setStrokeAnimationUrl(getCellValueAsString(currentRow.getCell(9)));
                vocabulary.setEtymologyStory(getCellValueAsString(currentRow.getCell(10)));
                vocabulary.setEtymologyImage(getCellValueAsString(currentRow.getCell(11)));

                if (meaningVi != null && !meaningVi.isEmpty()) {
                    String[] meaningParts = meaningVi.split(";");
                    for (String part : meaningParts) {
                        VocabularyMeaningRequest meaningReq = new VocabularyMeaningRequest();
                        meaningReq.setMeaning(part.trim());
                        meaningReq.setExampleSentenceCn(exampleCn);
                        meaningReq.setExampleSentenceVi(exampleVi);
                        meanings.add(meaningReq);
                    }
                }
                
                vocabulary.setMeanings(meanings);
                vocabularies.add(vocabulary);
            }

            workbook.close();
            
            // Sort: Single vocab first (TRUE > FALSE)
            // Note: Boolean.compare(true, false) -> 1, so B.compare(b, a) gives desc order (True first)
            vocabularies.sort((v1, v2) -> Boolean.compare(v2.getIsSingleVocab(), v1.getIsSingleVocab()));

            return vocabularies;
        } catch (IOException e) {
            throw new ApiException(ErrorCode.E227, "fail to parse Excel file: " + e.getMessage());
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
