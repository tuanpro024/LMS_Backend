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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                sheetModuleMap.putIfAbsent(row.getContentSheetName(), row.getModuleTypeEnum());
            }
        }

        log.info("Processing {} content sheets", sheetModuleMap.size());

        for (Map.Entry<String, ModuleType> entry : sheetModuleMap.entrySet()) {
            String sheetName = entry.getKey();
            ModuleType moduleType = entry.getValue();

            if (moduleType == null) {
                log.warn("Sheet '{}' has invalid module type, skipping", sheetName);
                continue;
            }

            ContentImportDetail detail = ContentImportDetail.builder()
                    .sheetName(sheetName)
                    .moduleType(moduleType)
                    .build();

            try {
                ContentReference ref = resolveContentForSheet(
                        sheetName, moduleType,
                        excelData.getContentSheetBytes().get(sheetName),
                        typeName, userId, isPrivate, detail);

                if (ref != null) {
                    refMap.put(sheetName, ref);
                    if (ref.getContentSetId() != null) {
                        result.getExternalContentSetIds().add(ref.getContentSetId());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process content sheet '{}': {}", sheetName, e.getMessage(), e);
                detail.setFailed(true);
                detail.setErrorMessage(e.getMessage());
                result.addError("Sheet '" + sheetName + "' (" + moduleType + "): " + e.getMessage());
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
            log.warn(error);
            return null;
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

        log.info("Sheet '{}': Creating new {} content", sheetName, moduleType);

        switch (moduleType) {
            case FLASHCARD: {
                MultipartFile sheetFile = LearningPathExcelParser.bytesToMultipartFile(sheetBytes, sheetName);
                ApiResponse<HierarchicalImportResult> resp = flashcardClient.importExcel(sheetFile, typeName);
                String setId = resp.data().getStudySetIds().get(0);
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
                String setId = resp.data().getStudySetIds().get(0);
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
                String setId = resp.data().getStudySetIds().get(0);
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
                String setId = resp.data().getStudySetIds().get(0);
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

                if (resp.data() == null || resp.data().getStudySetIds().isEmpty()) {
                    String error = "Quiz import returned no study set IDs";
                    detail.setFailed(true);
                    detail.setErrorMessage(error);
                    log.error(error);
                    return null;
                }

                String setId = resp.data().getStudySetIds().get(0);
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
                log.warn(error);
                return null;
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Invalid file format. Only .xlsx and .xls are supported");
        }
    }
}
