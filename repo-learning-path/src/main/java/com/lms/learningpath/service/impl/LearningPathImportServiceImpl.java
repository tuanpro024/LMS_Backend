package com.lms.learningpath.service.impl;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.entity.Type;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.TypeRepository;
import com.lms.learningpath.client.*;
import com.lms.learningpath.dto.excel.*;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.service.DuplicateContentChecker;
import com.lms.learningpath.service.LearningPathImportService;
import com.lms.learningpath.util.LearningPathExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Main implementation of Learning Path Excel import.
 * 
 * CRITICAL: This class has NO @Transactional annotation.
 * Phase 1 (processContentSheets) runs WITHOUT transaction - calls external
 * repos via Feign.
 * Phase 2 (buildLearningPathHierarchy) runs WITH transaction - delegated to
 * TransactionalHelper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathImportServiceImpl implements LearningPathImportService {

    private final LearningPathImportTransactionalHelper txHelper;
    private final FlashcardClient flashcardClient;
    private final WritingClient writingClient;
    private final KanjiOriginClient kanjiClient;
    private final PronunciationClient pronunciationClient;
    private final QuizClient quizClient;
    private final DuplicateContentChecker duplicateChecker;
    private final TypeRepository typeRepository;

    @Override
    public LearningPathImportResult importFromExcel(
            MultipartFile file,
            TypeName typeName,
            String userId,
            boolean isPrivate) {

        log.info("Starting Learning Path import for user: {} with typeName: {}", userId, typeName);

        // (1) Validate
        validateFile(file);
        Type packageType = typeRepository.findByName(typeName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid TypeName: " + typeName));

        // (2) Parse Excel
        LearningPathExcelData excelData;
        try {
            excelData = LearningPathExcelParser.parseMultiSheet(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        if (excelData.getStructureRows().isEmpty()) {
            throw new IllegalArgumentException("Structure sheet is empty or contains no data rows");
        }

        LearningPathImportResult result = LearningPathImportResult.builder().build();

        // ======== PHASE 1: NO TRANSACTION — Create content in external repos ========
        log.info("Phase 1: Processing content sheets (NO transaction)");
        Map<String, ContentReference> contentRefMap = processContentSheets(
                excelData, packageType.getName(), userId, isPrivate, result);

        // ======== PHASE 2: LOCAL TRANSACTION — Build learning path hierarchy ========
        log.info("Phase 2: Building learning path hierarchy (WITH transaction)");
        txHelper.buildLearningPathHierarchy(
                excelData.getStructureRows(), contentRefMap, packageType, userId, isPrivate, result);

        // Build summary message
        result.setMessage(String.format(
                "Import completed: %d package(s), %d folder(s), %d studySet(s), %d learningPath(s), %d step(s), %d module(s). "
                        +
                        "Content: %d created, %d reused, %d failed.",
                result.getTotalPackages(), result.getTotalFolders(), result.getTotalStudySets(),
                result.getTotalLearningPaths(), result.getTotalSteps(), result.getTotalModules(),
                result.getContentImportResults().values().stream().filter(ContentImportDetail::isNewlyCreated).count(),
                result.getContentImportResults().values().stream().filter(ContentImportDetail::isReused).count(),
                result.getContentImportResults().values().stream().filter(ContentImportDetail::isFailed).count()));

        log.info("Learning Path import completed: {}", result.getMessage());
        return result;
    }

    /**
     * PHASE 1 — NO @Transactional
     * Process content sheets, call Feign to create content in external repos.
     * Collect contentSetId references for Phase 2.
     */
    private Map<String, ContentReference> processContentSheets(
            LearningPathExcelData excelData,
            com.lms.content.common.entity.TypeName typeName,
            String userId,
            boolean isPrivate,
            LearningPathImportResult result) {

        Map<String, ContentReference> refMap = new LinkedHashMap<>();
        List<LearningPathStructureRow> rows = excelData.getStructureRows();

        // Collect unique (contentSheetName → moduleType) pairs
        Map<String, ModuleType> sheetModuleMap = new LinkedHashMap<>();
        for (LearningPathStructureRow row : rows) {
            if (row.hasModuleData() && row.getContentSheetName() != null && !row.getContentSheetName().isBlank()) {
                String normalizedSheetName = row.getContentSheetName().trim();
                ModuleType moduleType = row.getModuleTypeEnum();
                if (moduleType == null) {
                    throw new IllegalArgumentException(
                            "Invalid module type at row " + row.getRowNumber() + " for sheet '" + normalizedSheetName
                                    + "': " + row.getModuleType());
                }

                ModuleType existingModuleType = sheetModuleMap.get(normalizedSheetName);
                if (existingModuleType != null && existingModuleType != moduleType) {
                    throw new IllegalArgumentException(
                            "Conflicting module type for sheet '" + normalizedSheetName + "': "
                                    + existingModuleType + " vs " + moduleType + " (row " + row.getRowNumber() + ")");
                }
                sheetModuleMap.putIfAbsent(normalizedSheetName, moduleType);
            }
        }

        log.info("Processing {} content sheets", sheetModuleMap.size());

        for (Map.Entry<String, ModuleType> entry : sheetModuleMap.entrySet()) {
            String sheetName = entry.getKey();
            ModuleType moduleType = entry.getValue();

            ContentImportDetail detail = ContentImportDetail.builder()
                    .sheetName(sheetName)
                    .moduleType(moduleType)
                    .build();

            try {
                ContentReference ref = resolveContentForSheet(
                        sheetName, moduleType,
                        excelData.getContentSheetBytes().get(sheetName),
                        typeName, userId, isPrivate, detail);

                if (ref == null || ref.getContentSetId() == null || ref.getContentSetId().isBlank()) {
                    throw new IllegalStateException(
                            "No contentSetId returned for sheet '" + sheetName + "' (" + moduleType + ")");
                }

                refMap.put(sheetName, ref);
                if (ref.getContentSetId() != null) {
                    result.getExternalContentSetIds().add(ref.getContentSetId());
                }
            } catch (Exception e) {
                log.error("Failed to process content sheet '{}': {}", sheetName, e.getMessage(), e);
                detail.setFailed(true);
                detail.setErrorMessage(e.getMessage());
                result.addError("Sheet '" + sheetName + "' (" + moduleType + "): " + e.getMessage());
                result.getContentImportResults().put(sheetName, detail);
                throw new IllegalStateException(
                        "Import failed at content sheet '" + sheetName + "' (" + moduleType + "): " + e.getMessage(),
                        e);
            }

            result.getContentImportResults().put(sheetName, detail);
        }

        return refMap;
    }

    /**
     * Resolve content for a single sheet.
     * Priority:
     * 1) ExistingContentSetId from Excel → reuse immediately
     * 2) Extract studySetName from sheetBytes → check duplicate by studySetName +
     * domain criteria
     * 3) Create new content if no duplicate found
     */
    private ContentReference resolveContentForSheet(
            String sheetName,
            ModuleType moduleType,
            byte[] sheetBytes,
            TypeName typeName,
            String userId,
            boolean isPrivate,
            ContentImportDetail detail) throws IOException {

        // Priority 2: Check for duplicates by studySetName (extracted from sheetBytes)
        if (sheetBytes != null) {
            // Extract studySetName from sheetBytes
            Optional<String> studySetNameOpt = com.lms.learningpath.util.StudySetNameExtractor
                    .extractStudySetName(sheetBytes, moduleType);

            if (studySetNameOpt.isPresent()) {
                String normalizedStudySetName = studySetNameOpt.get();
                log.debug("Sheet '{}': Extracted studySetName: '{}'", sheetName, normalizedStudySetName);

                // Check for duplicate using studySetName
                Optional<String> duplicateId = duplicateChecker.findExistingContentSet(
                        normalizedStudySetName, moduleType);

                if (duplicateId.isPresent()) {
                    log.info("Sheet '{}': Found duplicate content by studySetName '{}', reusing ID: {}",
                            sheetName, normalizedStudySetName, duplicateId.get());
                    detail.setReused(true);
                    detail.setContentSetId(duplicateId.get());
                    return ContentReference.builder()
                            .moduleType(moduleType)
                            .contentSetId(duplicateId.get())
                            .newlyCreated(false)
                            .build();
                }

                log.debug("Sheet '{}': No duplicate found for studySetName '{}'", sheetName, normalizedStudySetName);
            } else {
                log.warn("Sheet '{}': Could not extract studySetName from sheetBytes, will create new content",
                        sheetName);
            }
        }

        // Priority 3: Create new content
        if (sheetBytes == null) {
            String error = "Content sheet '" + sheetName + "' not found in workbook";
            detail.setFailed(true);
            detail.setErrorMessage(error);
            throw new IllegalArgumentException(error);
        }

        return createContentInExternalRepo(sheetName, moduleType, sheetBytes, typeName, userId, detail);
    }

    /**
     * Create content in external repository via Feign.
     */
    private ContentReference createContentInExternalRepo(
            String sheetName,
            ModuleType moduleType,
            byte[] sheetBytes,
            TypeName typeName,
            String userId,
            ContentImportDetail detail) throws IOException {

        validateOutboundSheetPayload(sheetName, sheetBytes);
        log.info("Sheet '{}': Creating new {} content", sheetName, moduleType);

        switch (moduleType) {
            case FLASHCARD: {
                MultipartFile sheetFile = LearningPathExcelParser.bytesToMultipartFile(sheetBytes, sheetName);
                ApiResponse<HierarchicalImportResult> resp = flashcardClient.importExcel(sheetFile, typeName);
                String setId = extractFirstStudySetId(resp, sheetName, moduleType);
                detail.setNewlyCreated(true);
                detail.setContentSetId(setId);
                detail.setItemCount(resp.data().getTotalContentItems());
                log.info("Created flashcard content: {}", setId);
                return ContentReference.builder()
                        .moduleType(moduleType)
                        .contentSetId(setId)
                        .repoName("repo-flashcard")
                        .newlyCreated(true)
                        .build();
            }

            case WRITING: {
                MultipartFile sheetFile = LearningPathExcelParser.bytesToMultipartFile(sheetBytes, sheetName);
                ApiResponse<HierarchicalImportResult> resp = writingClient.importExcel(sheetFile, typeName);
                String setId = extractFirstStudySetId(resp, sheetName, moduleType);
                detail.setNewlyCreated(true);
                detail.setContentSetId(setId);
                detail.setItemCount(resp.data().getTotalContentItems());
                log.info("Created writing content: {}", setId);
                return ContentReference.builder()
                        .moduleType(moduleType)
                        .contentSetId(setId)
                        .repoName("repo-writing")
                        .newlyCreated(true)
                        .build();
            }

            case KANJI: {
                MultipartFile sheetFile = LearningPathExcelParser.bytesToMultipartFile(sheetBytes, sheetName);
                ApiResponse<HierarchicalImportResult> resp = kanjiClient.importExcel(sheetFile, typeName);
                String setId = extractFirstStudySetId(resp, sheetName, moduleType);
                detail.setNewlyCreated(true);
                detail.setContentSetId(setId);
                detail.setItemCount(resp.data().getTotalContentItems());
                log.info("Created kanji content: {}", setId);
                return ContentReference.builder()
                        .moduleType(moduleType)
                        .contentSetId(setId)
                        .repoName("repo-kanji-origin")
                        .newlyCreated(true)
                        .build();
            }

            case PRONUNCIATION: {
                MultipartFile sheetFile = LearningPathExcelParser.bytesToMultipartFile(sheetBytes, sheetName);
                ApiResponse<HierarchicalImportResult> resp = pronunciationClient.importExcel(sheetFile,
                        typeName);
                String setId = extractFirstStudySetId(resp, sheetName, moduleType);
                detail.setNewlyCreated(true);
                detail.setContentSetId(setId);
                detail.setItemCount(resp.data().getTotalContentItems());
                log.info("Created pronunciation content: {}", setId);
                return ContentReference.builder()
                        .moduleType(moduleType)
                        .contentSetId(setId)
                        .repoName("repo-pronunciation")
                        .newlyCreated(true)
                        .build();
            }

            case QUIZ: {
                MultipartFile sheetFile = LearningPathExcelParser.bytesToMultipartFile(sheetBytes, sheetName);
                ApiResponse<HierarchicalImportResult> resp = quizClient.importExcel(sheetFile, typeName);
                String setId = extractFirstStudySetId(resp, sheetName, moduleType);
                detail.setNewlyCreated(true);
                detail.setContentSetId(setId);
                detail.setItemCount(resp.data().getTotalContentItems());
                log.info("Created quiz content: {}", setId);
                return ContentReference.builder()
                        .moduleType(moduleType)
                        .contentSetId(setId)
                        .repoName("repo-quiz")
                        .newlyCreated(true)
                        .build();
            }

            default: {
                String error = "Unsupported module type for import: " + moduleType;
                detail.setFailed(true);
                detail.setErrorMessage(error);
                throw new IllegalArgumentException(error);
            }
        }
    }

    private String extractFirstStudySetId(
            ApiResponse<HierarchicalImportResult> response,
            String sheetName,
            ModuleType moduleType) {

        if (response == null || response.data() == null) {
            throw new IllegalStateException(
                    "External import returned empty response for sheet '" + sheetName + "' (" + moduleType + ")");
        }

        List<String> studySetIds = Optional.ofNullable(response.data().getStudySetIds())
                .orElse(Collections.emptyList());
        if (studySetIds.isEmpty() || studySetIds.get(0) == null || studySetIds.get(0).isBlank()) {
            throw new IllegalStateException(
                    "External import returned no studySetId for sheet '" + sheetName + "' (" + moduleType + ")");
        }

        return studySetIds.get(0);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Invalid file format. Only .xlsx is supported");
        }
    }

    /**
     * Defensive validation to prevent uploading a Structure-like sheet to external
     * repos.
     */
    private void validateOutboundSheetPayload(String sheetName, byte[] sheetBytes) {
        if (sheetBytes == null || sheetBytes.length == 0) {
            throw new IllegalArgumentException("Content sheet '" + sheetName + "' is empty");
        }

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(sheetBytes))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Content sheet '" + sheetName + "' has no sheet data for external import");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) {
                return;
            }

            String colC = readHeader(header, 2);
            String colM = readHeader(header, 12); // ModuleType in Structure template
            String colP = readHeader(header, 15); // ContentSheetName in Structure template

            if ("ModuleType".equalsIgnoreCase(colM) && "ContentSheetName".equalsIgnoreCase(colP)) {
                throw new IllegalArgumentException(
                        "Content sheet '" + sheetName + "' was detected as Structure schema; import aborted");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to validate content sheet '" + sheetName + "' before external import: " + e.getMessage(),
                    e);
        }
    }

    private String readHeader(Row row, int col) {
        if (row.getCell(col) == null) {
            return "";
        }
        try {
            return row.getCell(col).getStringCellValue().trim();
        } catch (Exception e) {
            try {
                return String.valueOf((long) row.getCell(col).getNumericCellValue()).trim();
            } catch (Exception ignored) {
                return "";
            }
        }
    }
}
