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

import java.util.Iterator;
import java.util.List;

@Slf4j
public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String SHEET = "Vocabularies";
    public static final String SPLIT_CHAR = "\\|";

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
                sheet = workbook.getSheetAt(0);
            }

            Iterator<Row> rows = sheet.iterator();

            List<CreateVocabularyRequest> vocabularies = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

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
                String meaningViStr = getCellValueAsString(currentRow.getCell(2));

                String exampleCnStr = getCellValueAsString(currentRow.getCell(3));
                String exampleViStr = getCellValueAsString(currentRow.getCell(4));

                String hskStr = getCellValueAsString(currentRow.getCell(5));
                if (!hskStr.isEmpty()) {
                    try {
                        vocabulary.setHskLevel((int) Double.parseDouble(hskStr));
                    } catch (NumberFormatException e) {
                    }
                }

                String isSingleStr = getCellValueAsString(currentRow.getCell(6));
                vocabulary.setIsSingleVocab(Boolean.parseBoolean(isSingleStr) || "TRUE".equalsIgnoreCase(isSingleStr));

                String componentsStr = getCellValueAsString(currentRow.getCell(7));
                if (componentsStr != null && !componentsStr.isEmpty()) {
                    List<VocabComponentRequest> components = new ArrayList<>();
                    String[] comps = componentsStr.split(SPLIT_CHAR);
                    int order = 1;
                    for (String comp : comps) {
                        String[] parts = comp.split(":");
                        String cHanzi = parts[0].trim();
                        String cPinyin = parts.length > 1 ? parts[1].trim() : "";

                        CreateVocabularyRequest compReq = new CreateVocabularyRequest();
                        compReq.setHanzi(cHanzi);
                        compReq.setPinyin(cPinyin);
                        compReq.setIsSingleVocab(true);

                        components.add(VocabComponentRequest.builder()
                                .newVocabulary(compReq)
                                .orderIndex(order++)
                                .build());
                    }
                    vocabulary.setComponents(components);
                }

                vocabulary.setAudioUrl(getCellValueAsString(currentRow.getCell(8)));
                vocabulary.setStrokeAnimationUrl(getCellValueAsString(currentRow.getCell(9)));
                vocabulary.setEtymologyStory(getCellValueAsString(currentRow.getCell(10)));
                vocabulary.setEtymologyImage(getCellValueAsString(currentRow.getCell(11)));
                vocabulary.setImageUrl(getCellValueAsString(currentRow.getCell(12)));

                String wordTypeStr = getCellValueAsString(currentRow.getCell(13));
                String examplePinyinStr = getCellValueAsString(currentRow.getCell(14));
                String exampleEnStr = getCellValueAsString(currentRow.getCell(15));

                if (meaningViStr != null && !meaningViStr.isEmpty()) {
                    String[] meaningParts = meaningViStr.split(SPLIT_CHAR);
                    String[] wordTypeParts = wordTypeStr.split(SPLIT_CHAR);
                    String[] exCnParts = exampleCnStr.split(SPLIT_CHAR);
                    String[] exViParts = exampleViStr.split(SPLIT_CHAR);
                    String[] exPinyinParts = examplePinyinStr.split(SPLIT_CHAR);
                    String[] exEnParts = exampleEnStr.split(SPLIT_CHAR);

                    List<String> collectedWordTypes = new ArrayList<>();
                    if (wordTypeParts != null && wordTypeParts.length > 0) {
                        for (String wt : wordTypeParts) {
                            if (!wt.trim().isEmpty()) {
                                collectedWordTypes.add(wt.trim());
                            }
                        }
                    }
                    List<String> distinctWordTypes = new ArrayList<>();
                    for (String wt : collectedWordTypes) {
                        if (!distinctWordTypes.contains(wt)) {
                            distinctWordTypes.add(wt);
                        }
                    }
                    vocabulary.setWordTypes(distinctWordTypes);

                    for (int i = 0; i < meaningParts.length; i++) {
                        VocabularyMeaningRequest meaningReq = new VocabularyMeaningRequest();
                        meaningReq.setMeaning(meaningParts[i].trim());

                        meaningReq.setExampleSentenceCn(getValueAtIndexOrFirst(exCnParts, i));
                        meaningReq.setExampleSentenceVi(getValueAtIndexOrFirst(exViParts, i));
                        meaningReq.setExampleSentencePinyin(getValueAtIndexOrFirst(exPinyinParts, i));
                        meaningReq.setExampleSentenceEn(getValueAtIndexOrFirst(exEnParts, i));

                        meanings.add(meaningReq);
                    }
                }

                vocabulary.setMeanings(meanings);
                vocabularies.add(vocabulary);
            }

            workbook.close();

            // Sort: Single vocab first (TRUE > FALSE)
            // Note: Boolean.compare(true, false) -> 1, so B.compare(b, a) gives desc order
            // (True first)
            vocabularies.sort((v1, v2) -> Boolean.compare(v2.getIsSingleVocab(), v1.getIsSingleVocab()));

            return vocabularies;
        } catch (IOException e) {
            throw new ApiException(ErrorCode.E227, "fail to parse Excel file: " + e.getMessage());
        }
    }

    private static String getValueAtIndexOrFirst(String[] parts, int index) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        if (index < parts.length) {
            return parts[index].trim();
        }
        if (parts.length == 1) {
            return parts[0].trim();
        }
        return "";
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
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
