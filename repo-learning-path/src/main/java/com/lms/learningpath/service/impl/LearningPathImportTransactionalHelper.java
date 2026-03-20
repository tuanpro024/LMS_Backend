package com.lms.learningpath.service.impl;

import com.lms.content.common.entity.Folder;
import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.entity.Type;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.learningpath.dto.excel.ContentReference;
import com.lms.learningpath.dto.excel.LearningPathImportResult;
import com.lms.learningpath.dto.excel.LearningPathStructureRow;
import com.lms.learningpath.entity.LearningPath;
import com.lms.learningpath.entity.Step;
import com.lms.learningpath.entity.StepModule;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.repository.LearningPathRepository;
import com.lms.learningpath.repository.StepModuleRepository;
import com.lms.learningpath.repository.StepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transaction helper for Phase 2 of Learning Path import.
 * 
 * CRITICAL: Must be a separate @Component class.
 * Spring AOP only intercepts method calls from OUTSIDE the class.
 * If this were in the same class as LearningPathImportServiceImpl,
 * calling this method would bypass the @Transactional proxy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LearningPathImportTransactionalHelper {

    private final PackageRepository packageRepository;
    private final FolderRepository folderRepository;
    private final StudySetRepository studySetRepository;
    private final LearningPathRepository learningPathRepository;
    private final StepRepository stepRepository;
    private final StepModuleRepository stepModuleRepository;

    /**
     * PHASE 2 — @Transactional
     * Build complete learning path hierarchy in local database.
     * If any step fails → entire transaction rolls back.
     * External content from Phase 1 remains untouched.
     * 
     * @param rows          Structure rows from Excel
     * @param contentRefMap Map of sheetName → ContentReference from Phase 1
     * @param packageType   Package type entity
     * @param userId        User ID
     * @param isPrivate     Privacy flag
     * @param result        Result object to populate
     */
    @Transactional
    public void buildLearningPathHierarchy(
            List<LearningPathStructureRow> rows,
            Map<String, ContentReference> contentRefMap,
            Type packageType,
            String userId,
            boolean isPrivate,
            LearningPathImportResult result) {

        log.info("Building learning path hierarchy (TRANSACTIONAL Phase 2)");

        // Current context tracking (similar to AbstractHierarchicalImportService)
        Package currentPackage = null;
        Folder currentFolder = null;
        StudySet currentStudySet = null;
        LearningPath currentLearningPath = null;
        Step currentStep = null;
        int autoStepOrder = 0;
        int autoModuleOrder = 0;

        for (LearningPathStructureRow row : rows) {
            if (row.isEmpty())
                continue;

            // --- Package ---
            if (row.hasPackageData()) {
                currentPackage = Package.builder()
                        .name(row.getPackageName().trim())
                        .description(row.getPackageDescription() != null ? row.getPackageDescription().trim() : null)
                        .type(packageType)
                        .userId(userId)
                        .subjects(new ArrayList<>())
                        .folders(new ArrayList<>())
                        .build();
                currentPackage = packageRepository.save(currentPackage);
                result.getPackageIds().add(currentPackage.getId());
                result.setTotalPackages(result.getTotalPackages() + 1);
                log.info("Created Package: {} (ID: {})", currentPackage.getName(), currentPackage.getId());

                // Reset context
                currentFolder = null;
                currentStudySet = null;
                currentLearningPath = null;
                currentStep = null;
                autoStepOrder = 0;
                autoModuleOrder = 0;
            }

            // --- Folder ---
            if (row.hasFolderData()) {
                if (currentPackage == null) {
                    throw new IllegalArgumentException(
                            "Invalid structure at row " + row.getRowNumber() + ": Folder without Package");
                }
                currentFolder = Folder.builder()
                        .name(row.getFolderName().trim())
                        .description(row.getFolderDescription() != null ? row.getFolderDescription().trim() : null)
                        .packageEntity(currentPackage)
                        .userId(userId)
                        .isPrivate(isPrivate)
                        .studySets(new ArrayList<>())
                        .build();
                currentFolder = folderRepository.save(currentFolder);
                result.getFolderIds().add(currentFolder.getId());
                result.setTotalFolders(result.getTotalFolders() + 1);
                log.info("Created Folder: {} (ID: {})", currentFolder.getName(), currentFolder.getId());

                // Reset context
                currentStudySet = null;
                currentLearningPath = null;
                currentStep = null;
                autoStepOrder = 0;
                autoModuleOrder = 0;
            }

            // --- StudySet ---
            if (row.hasStudySetData()) {
                if (currentFolder == null) {
                    throw new IllegalArgumentException(
                            "Invalid structure at row " + row.getRowNumber() + ": StudySet without Folder");
                }
                currentStudySet = StudySet.builder()
                        .title(row.getStudySetName().trim())
                        .description(row.getStudySetDescription() != null ? row.getStudySetDescription().trim() : null)
                        .userId(userId)
                        .isPrivate(isPrivate)
                        .build();
                currentStudySet = studySetRepository.save(currentStudySet);
                currentFolder.addStudySet(currentStudySet);
                folderRepository.save(currentFolder);
                result.getStudySetIds().add(currentStudySet.getId());
                result.setTotalStudySets(result.getTotalStudySets() + 1);
                log.info("Created StudySet: {} (ID: {})", currentStudySet.getTitle(), currentStudySet.getId());

                // Reset context
                currentLearningPath = null;
                currentStep = null;
                autoStepOrder = 0;
                autoModuleOrder = 0;
            }

            // --- LearningPath ---
            if (row.hasLearningPathData()) {
                if (currentStudySet == null) {
                    throw new IllegalArgumentException(
                            "Invalid structure at row " + row.getRowNumber() + ": LearningPath without StudySet");
                }
                currentLearningPath = LearningPath.builder()
                        .title(row.getLearningPathTitle().trim())
                        .description(row.getLearningPathDescription() != null ? row.getLearningPathDescription().trim()
                                : null)
                        .createdBy(userId)
                        .isActive(true)
                        .build();
                currentLearningPath.setStudySet(currentStudySet);
                currentLearningPath = learningPathRepository.save(currentLearningPath);
                result.getLearningPathIds().add(currentLearningPath.getId());
                result.setTotalLearningPaths(result.getTotalLearningPaths() + 1);
                log.info("Created LearningPath: {} (ID: {})", currentLearningPath.getTitle(),
                        currentLearningPath.getId());

                // Reset context
                currentStep = null;
                autoStepOrder = 0;
                autoModuleOrder = 0;
            }

            // --- Step ---
            if (row.hasStepData()) {
                if (currentLearningPath == null) {
                    throw new IllegalArgumentException(
                            "Invalid structure at row " + row.getRowNumber() + ": Step without LearningPath");
                }
                autoStepOrder++;
                currentStep = Step.builder()
                        .learningPathId(currentLearningPath.getId())
                        .title(row.getStepTitle().trim())
                        .description(row.getStepDescription() != null ? row.getStepDescription().trim() : null)
                        .stepOrder(row.getStepOrder() != null ? row.getStepOrder() : autoStepOrder)
                        .isRequired(true)
                        .isActive(true)
                        .build();
                currentStep = stepRepository.save(currentStep);
                result.getStepIds().add(currentStep.getId());
                result.setTotalSteps(result.getTotalSteps() + 1);
                log.info("Created Step: {} (ID: {})", currentStep.getTitle(), currentStep.getId());

                // Reset module order
                autoModuleOrder = 0;
            }

            // --- Module ---
            if (row.hasModuleData()) {
                if (currentStep == null) {
                    throw new IllegalArgumentException(
                            "Invalid structure at row " + row.getRowNumber() + ": Module without Step");
                }
                autoModuleOrder++;

                String contentSetId = resolveContentSetId(row, contentRefMap, currentStudySet, result);
                ModuleType modType = row.getModuleTypeEnum();

                if (modType == null) {
                    throw new IllegalArgumentException(
                            "Invalid module type at row " + row.getRowNumber() + ": " + row.getModuleType());
                }

                if (contentSetId == null || contentSetId.isBlank()) {
                    throw new IllegalStateException(
                            "Missing contentSetId at row " + row.getRowNumber() +
                                    " for sheet '" + row.getContentSheetName() + "'");
                }

                StepModule module = StepModule.builder()
                        .stepId(currentStep.getId())
                        .moduleType(modType)
                        .moduleOrder(row.getModuleOrder() != null ? row.getModuleOrder() : autoModuleOrder)
                        .title(row.getModuleTitle().trim())
                        .contentSetId(contentSetId)
                        .isRequired(row.getIsRequired() != null ? row.getIsRequired() : true)
                        .isActive(true)
                        .build();
                module = stepModuleRepository.save(module);
                result.getStepModuleIds().add(module.getId());
                result.setTotalModules(result.getTotalModules() + 1);
                log.info("Created StepModule: {} (ID: {}, contentSetId: {})", module.getTitle(), module.getId(),
                        contentSetId);
            }
        }

        log.info(
                "Phase 2 complete: Created {} packages, {} folders, {} studySets, {} learningPaths, {} steps, {} modules",
                result.getTotalPackages(), result.getTotalFolders(), result.getTotalStudySets(),
                result.getTotalLearningPaths(), result.getTotalSteps(), result.getTotalModules());
    }

    /**
     * Resolve contentSetId for a module.
     * Priority: 1) ExistingContentSetId, 2) ContentReference from Phase 1, 3)
     * Create quiz if needed.
     */
    private String resolveContentSetId(
            LearningPathStructureRow row,
            Map<String, ContentReference> refMap,
            StudySet currentStudySet,
            LearningPathImportResult result) {

        // Priority 2: Lookup from Phase 1 contentRefMap
        if (row.getContentSheetName() != null && !row.getContentSheetName().isBlank()) {
            ContentReference ref = refMap.get(row.getContentSheetName().trim());
            if (ref != null && ref.getContentSetId() != null) {
                return ref.getContentSetId();
            }
        }

        // Priority 3: null (placeholder or external reference)
        return null;
    }

}
