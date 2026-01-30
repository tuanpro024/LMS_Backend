package com.lms.learningpath.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.dto.response.SectionModuleResponse;
import com.lms.learningpath.entity.SectionModule;
import com.lms.learningpath.entity.UserLearningProgress;
import com.lms.learningpath.entity.UserSectionProgress;
import com.lms.learningpath.repository.SectionModuleRepository;
import com.lms.learningpath.repository.UserLearningProgressRepository;
import com.lms.learningpath.repository.UserSectionProgressRepository;
import com.lms.learningpath.service.ModuleIntegrationService;
import com.lms.learningpath.service.ProgressTrackingService;
import com.lms.learningpath.service.UnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ProgressTrackingService.
 * Tracks user completion of modules, sections, and learning paths.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private final SectionModuleRepository moduleRepository;
    private final UserSectionProgressRepository sectionProgressRepository;
    private final UserLearningProgressRepository learningProgressRepository;
    private final FolderRepository folderRepository;
    private final PackageRepository packageRepository;
    private final ModuleIntegrationService moduleIntegrationService;
    private final UnlockService unlockService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void completeModule(String userId, String moduleId) {
        Optional<SectionModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            log.warn("Module not found: {}", moduleId);
            return;
        }

        SectionModule module = moduleOpt.get();
        String folderId = module.getFolderId();

        // Get or create section progress
        UserSectionProgress progress = sectionProgressRepository
                .findByUserIdAndFolderId(userId, folderId)
                .orElse(UserSectionProgress.builder()
                        .userId(userId)
                        .folderId(folderId)
                        .completedModuleIds("[]")
                        .completionPercentage(0.0)
                        .isCompleted(false)
                        .build());

        // Add module to completed list
        List<String> completedIds = parseJsonArray(progress.getCompletedModuleIds());
        if (!completedIds.contains(moduleId)) {
            completedIds.add(moduleId);
            progress.setCompletedModuleIds(toJsonArray(completedIds));
            progress.setLastAccessedAt(LocalDateTime.now());

            // Update progress
            updateSectionProgress(userId, folderId);
        }
    }

    @Override
    @Transactional
    public void updateSectionProgress(String userId, String folderId) {
        UserSectionProgress progress = sectionProgressRepository
                .findByUserIdAndFolderId(userId, folderId)
                .orElse(UserSectionProgress.builder()
                        .userId(userId)
                        .folderId(folderId)
                        .completedModuleIds("[]")
                        .completionPercentage(0.0)
                        .isCompleted(false)
                        .build());

        // Calculate completion percentage
        List<SectionModule> allModules = moduleRepository.findByFolderIdOrderByModuleOrderAsc(folderId);
        List<SectionModule> requiredModules = allModules.stream()
                .filter(SectionModule::getIsRequired)
                .toList();

        if (requiredModules.isEmpty()) {
            progress.setCompletionPercentage(0.0);
            sectionProgressRepository.save(progress);
            return;
        }

        List<String> completedIds = parseJsonArray(progress.getCompletedModuleIds());
        long completedRequiredCount = requiredModules.stream()
                .filter(m -> completedIds.contains(m.getId()))
                .count();

        double percentage = (completedRequiredCount * 100.0) / requiredModules.size();
        progress.setCompletionPercentage(percentage);

        // Check if section is completed
        if (percentage >= 100.0 && !progress.getIsCompleted()) {
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());

            // Trigger unlock for next section
            String packageId = folderRepository.findById(folderId)
                    .map(f -> f.getPackageEntity().getId())
                    .orElse(null);
            if (packageId != null) {
                unlockService.unlockNextSection(userId, packageId, folderId);
                updateLearningPathProgress(userId, packageId);
            }
        }

        sectionProgressRepository.save(progress);
    }

    @Override
    public boolean isSectionCompleted(String userId, String folderId) {
        return sectionProgressRepository.existsByUserIdAndFolderIdAndIsCompletedTrue(userId, folderId);
    }

    @Override
    public double getSectionCompletionPercentage(String userId, String folderId) {
        return sectionProgressRepository.findByUserIdAndFolderId(userId, folderId)
                .map(UserSectionProgress::getCompletionPercentage)
                .orElse(0.0);
    }

    @Override
    public double getLearningPathCompletionPercentage(String userId, String packageId) {
        return learningProgressRepository.findByUserIdAndPackageId(userId, packageId)
                .map(UserLearningProgress::getCompletionPercentage)
                .orElse(0.0);
    }

    @Override
    @Transactional
    public void initializeLearningPathProgress(String userId, String packageId) {
        if (learningProgressRepository.findByUserIdAndPackageId(userId, packageId).isEmpty()) {
            UserLearningProgress progress = UserLearningProgress.builder()
                    .userId(userId)
                    .packageId(packageId)
                    .completedFolderIds("[]")
                    .completionPercentage(0.0)
                    .startedAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .build();
            learningProgressRepository.save(progress);
            log.info("Initialized learning path progress for user {} on package {}", userId, packageId);
        }
    }

    @Override
    public List<SectionModuleResponse> getModulesWithProgress(String userId, String folderId) {
        List<SectionModule> modules = moduleRepository.findByFolderIdOrderByModuleOrderAsc(folderId);
        UserSectionProgress progress = sectionProgressRepository
                .findByUserIdAndFolderId(userId, folderId)
                .orElse(null);

        List<String> completedIds = progress != null
                ? parseJsonArray(progress.getCompletedModuleIds())
                : new ArrayList<>();

        return modules.stream().map(module -> {
            SectionModuleResponse response = SectionModuleResponse.builder()
                    .id(module.getId())
                    .folderId(module.getFolderId())
                    .moduleType(module.getModuleType())
                    .studySetId(module.getStudySetId())
                    .moduleOrder(module.getModuleOrder())
                    .displayTitle(module.getDisplayTitle())
                    .isRequired(module.getIsRequired())
                    .moduleDescription(module.getModuleDescription())
                    .build();

            // Fetch StudySet data from external service
            Optional<StudySetDto> studySetOpt = moduleIntegrationService.getStudySet(
                    module.getModuleType(), module.getStudySetId());

            if (studySetOpt.isPresent()) {
                StudySetDto studySet = studySetOpt.get();
                response.setStudySetTitle(studySet.getTitle());
                response.setStudySetDescription(studySet.getDescription());
                response.setStudySetThumbnail(studySet.getThumbnail());
                response.setEstimatedMinutes(studySet.getEstimatedMinutes());
                response.setItemCount(studySet.getItemCount());
                response.setIsAvailable(true);
            } else {
                response.setIsAvailable(false);
                log.warn("StudySet {} not available from module {}",
                        module.getStudySetId(), module.getModuleType());
            }

            return response;
        }).collect(Collectors.toList());
    }

    private void updateLearningPathProgress(String userId, String packageId) {
        UserLearningProgress progress = learningProgressRepository
                .findByUserIdAndPackageId(userId, packageId)
                .orElse(null);

        if (progress == null) {
            return;
        }

        // Get all folders in package
        List<String> allFolderIds = folderRepository.findByPackageEntityId(packageId).stream()
                .map(f -> f.getId())
                .toList();

        if (allFolderIds.isEmpty()) {
            return;
        }

        // Get completed folders
        List<String> completedFolderIds = allFolderIds.stream()
                .filter(folderId -> isSectionCompleted(userId, folderId))
                .toList();

        // Update progress
        progress.setCompletedFolderIds(toJsonArray(completedFolderIds));
        double percentage = (completedFolderIds.size() * 100.0) / allFolderIds.size();
        progress.setCompletionPercentage(percentage);
        progress.setLastAccessedAt(LocalDateTime.now());

        if (percentage >= 100.0 && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
            log.info("User {} completed learning path {}", userId, packageId);
        }

        learningProgressRepository.save(progress);
    }

    private List<String> parseJsonArray(String json) {
        try {
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON array: {}", json, e);
            return new ArrayList<>();
        }
    }

    private String toJsonArray(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert list to JSON", e);
            return "[]";
        }
    }
}
