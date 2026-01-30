package com.lms.learningpath.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.UpdateProgressRequest;
import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.dto.response.StudySetModuleResponse;
import com.lms.learningpath.dto.response.StudySetProgressDto;
import com.lms.learningpath.entity.ModuleProgress;
import com.lms.learningpath.entity.StudySetModule;
import com.lms.learningpath.entity.StudySetProgress;
import com.lms.learningpath.entity.enums.ProgressStatus;
import com.lms.learningpath.repository.ModuleProgressRepository;
import com.lms.learningpath.repository.StudySetModuleRepository;
import com.lms.learningpath.repository.StudySetProgressRepository;
import com.lms.learningpath.service.IModuleIntegrationService;
import com.lms.learningpath.service.IProgressTrackingService;
import com.lms.learningpath.service.IRealtimeNotificationService;
import com.lms.learningpath.service.IUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressTrackingServiceImpl implements IProgressTrackingService {

    private final ModuleProgressRepository moduleProgressRepo;
    private final StudySetProgressRepository studySetProgressRepo;
    private final StudySetModuleRepository studySetModuleRepo;
    private final StudySetRepository studySetRepo;
    private final IModuleIntegrationService integrationService;
    private final IRealtimeNotificationService realtimeService;
    private final IUnlockService unlockService;

    /**
     * Start a module
     */
    @Override
    @Transactional
    public ModuleProgressDto startModule(String userId, String moduleId) {
        StudySetModule module = studySetModuleRepo.findById(moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Module not found"));

        // Check if StudySet is unlocked
        if (!unlockService.isStudySetUnlocked(userId, module.getStudySetId())) {
            throw new ApiException(ErrorCode.E240, "StudySet is locked");
        }

        // Check existing progress
        ModuleProgress existing = moduleProgressRepo
                .findByUserIdAndStudySetModuleId(userId, moduleId)
                .orElse(null);

        if (existing != null && existing.getStatus() == ProgressStatus.COMPLETED) {
            throw new ApiException(ErrorCode.E221, "Module already completed");
        }

        // Get total items from content set
        int totalItems = integrationService.getContentSetItemCount(
                module.getModuleType(),
                module.getContentSetId());

        ModuleProgress progress;
        if (existing == null) {
            progress = ModuleProgress.builder()
                    .userId(userId)
                    .studySetModuleId(moduleId)
                    .studySetId(module.getStudySetId())
                    .status(ProgressStatus.IN_PROGRESS)
                    .completedItems(0)
                    .totalItems(totalItems)
                    .studyTimeSeconds(0)
                    .startedAt(Instant.now())
                    .build();
        } else {
            existing.setStatus(ProgressStatus.IN_PROGRESS);
            existing.setStartedAt(Instant.now());
            existing.setTotalItems(totalItems);
            progress = existing;
        }

        progress = moduleProgressRepo.save(progress);

        // Update StudySet progress
        updateStudySetProgress(userId, module.getStudySetId());

        // Send realtime notification
        realtimeService.notifyProgressUpdate(userId, toDto(progress));

        return toDto(progress);
    }

    /**
     * Update module progress
     */
    @Override
    @Transactional
    public ModuleProgressDto updateProgress(String userId, String moduleId, UpdateProgressRequest request) {
        ModuleProgress progress = moduleProgressRepo
                .findByUserIdAndStudySetModuleId(userId, moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Progress not found. Start module first."));

        if (request.getCompletedItems() != null) {
            progress.setCompletedItems(request.getCompletedItems());
        }
        if (request.getStudyTimeSeconds() != null) {
            progress.setStudyTimeSeconds(progress.getStudyTimeSeconds() + request.getStudyTimeSeconds());
        }
        if (request.getScore() != null) {
            progress.setScore(request.getScore());
        }
        if (request.getMetadata() != null) {
            progress.setMetadata(request.getMetadata());
        }

        progress = moduleProgressRepo.save(progress);

        // Send realtime update
        realtimeService.notifyProgressUpdate(userId, toDto(progress));

        return toDto(progress);
    }

    /**
     * Complete a module
     */
    @Override
    @Transactional
    public ModuleProgressDto completeModule(String userId, String moduleId, CompleteModuleRequest request) {
        ModuleProgress progress = moduleProgressRepo
                .findByUserIdAndStudySetModuleId(userId, moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Progress not found"));

        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            throw new ApiException(ErrorCode.E221, "Module already completed");
        }

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedItems(progress.getTotalItems());
        progress.setCompletedAt(Instant.now());

        if (request.getScore() != null) {
            progress.setScore(request.getScore());
        }
        if (request.getTotalStudyTimeSeconds() != null) {
            progress.setStudyTimeSeconds(request.getTotalStudyTimeSeconds());
        }
        if (request.getMetadata() != null) {
            progress.setMetadata(request.getMetadata());
        }

        progress = moduleProgressRepo.save(progress);

        // Update StudySet progress
        StudySetModule module = studySetModuleRepo.findById(moduleId).orElseThrow();
        StudySetProgress setProgress = updateStudySetProgress(userId, module.getStudySetId());

        // Send realtime notifications
        realtimeService.notifyModuleCompleted(userId, toDto(progress));

        // If StudySet completed, check unlock
        if (setProgress.canUnlockNext()) {
            StudySet studySet = studySetRepo.findById(module.getStudySetId()).orElseThrow();
            realtimeService.notifyStudySetCompleted(userId, studySet.getId());

            // Check and unlock next StudySet
            unlockService.checkAndUnlockNextStudySet(userId, studySet);
        }

        return toDto(progress);
    }

    /**
     * Get modules with progress for a StudySet
     */
    @Override
    @Transactional(readOnly = true)
    public List<StudySetModuleResponse> getModulesWithProgress(String userId, String studySetId) {
        List<StudySetModule> modules = studySetModuleRepo.findByStudySetIdOrderByModuleOrder(studySetId);
        List<String> moduleIds = modules.stream().map(StudySetModule::getId).collect(Collectors.toList());
        List<ModuleProgress> progressList = moduleProgressRepo.findByUserIdAndModuleIds(userId, moduleIds);

        return modules.stream().map(module -> {
            var response = StudySetModuleResponse.builder()
                    .id(module.getId())
                    .studySetId(module.getStudySetId())
                    .moduleType(module.getModuleType())
                    .moduleOrder(module.getModuleOrder())
                    .title(module.getTitle())
                    .description(module.getDescription())
                    .icon(module.getIcon())
                    .color(module.getColor())
                    .contentSetId(module.getContentSetId())
                    .estimatedMinutes(module.getEstimatedMinutes())
                    .isRequired(module.getIsRequired())
                    .build();

            // Enrich with content set details
            if (module.getContentSetId() != null) {
                integrationService.getStudySet(module.getModuleType(), module.getContentSetId())
                        .ifPresent(response::setContentSetDetails);
            }

            // Add user progress
            progressList.stream()
                    .filter(p -> p.getStudySetModuleId().equals(module.getId()))
                    .findFirst()
                    .ifPresent(p -> response.setUserProgress(toDto(p)));

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * Get StudySet progress with lock status
     */
    @Override
    @Transactional(readOnly = true)
    public StudySetProgressDto getStudySetProgress(String userId, String studySetId) {
        StudySet studySet = studySetRepo.findById(studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        StudySetProgress progress = studySetProgressRepo.findByUserIdAndStudySetId(userId, studySetId)
                .orElse(createDefaultProgress(userId, studySet));

        boolean isLocked = !unlockService.isStudySetUnlocked(userId, studySetId);
        String lockReason = isLocked ? unlockService.getLockReason(userId, studySetId) : null;

        return StudySetProgressDto.builder()
                .id(progress.getId())
                .studySetId(progress.getStudySetId())
                .studySetTitle(studySet.getTitle())
                .folderId(progress.getFolderId())
                .status(progress.getStatus())
                .completedModules(progress.getCompletedModules())
                .totalModules(progress.getTotalModules())
                .requiredCompletedModules(progress.getRequiredCompletedModules())
                .totalRequiredModules(progress.getTotalRequiredModules())
                .progressPercentage(progress.getProgressPercentage())
                .isLocked(isLocked)
                .lockReason(lockReason)
                .firstStartedAt(progress.getFirstStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }

    /**
     * Update StudySet progress based on module completions
     */
    private StudySetProgress updateStudySetProgress(String userId, String studySetId) {
        StudySet studySet = studySetRepo.findById(studySetId).orElseThrow();

        StudySetProgress progress = studySetProgressRepo.findByUserIdAndStudySetId(userId, studySetId)
                .orElse(createDefaultProgress(userId, studySet));

        // Count modules
        long totalModules = studySetModuleRepo.countByStudySetIdAndIsActive(studySetId, true);
        long totalRequired = studySetModuleRepo.countByStudySetIdAndIsRequiredAndIsActive(studySetId, true, true);
        long completedModules = moduleProgressRepo.countByUserIdAndStudySetIdAndStatus(
                userId, studySetId, ProgressStatus.COMPLETED);

        // Count completed required modules
        List<StudySetModule> requiredModules = studySetModuleRepo.findByStudySetIdOrderByModuleOrder(studySetId)
                .stream()
                .filter(StudySetModule::getIsRequired)
                .collect(Collectors.toList());

        List<String> requiredModuleIds = requiredModules.stream()
                .map(StudySetModule::getId)
                .collect(Collectors.toList());

        long completedRequired = requiredModuleIds.isEmpty() ? 0
                : moduleProgressRepo.findByUserIdAndModuleIds(userId, requiredModuleIds)
                        .stream()
                        .filter(ModuleProgress::isCompleted)
                        .count();

        progress.setTotalModules((int) totalModules);
        progress.setTotalRequiredModules((int) totalRequired);
        progress.setCompletedModules((int) completedModules);
        progress.setRequiredCompletedModules((int) completedRequired);

        if (progress.getFirstStartedAt() == null && completedModules > 0) {
            progress.setFirstStartedAt(Instant.now());
        }

        if (completedModules > 0 && !progress.canUnlockNext()) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        } else if (progress.canUnlockNext() && totalRequired > 0) {
            progress.setStatus(ProgressStatus.COMPLETED);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(Instant.now());
            }
        }

        return studySetProgressRepo.save(progress);
    }

    private StudySetProgress createDefaultProgress(String userId, StudySet studySet) {
        // Get folder from studySet (via many-to-many)
        String folderId = studySet.getFolders().isEmpty() ? null : studySet.getFolders().get(0).getId();
        String packageId = folderId != null && !studySet.getFolders().isEmpty()
                ? studySet.getFolders().get(0).getPackageEntity().getId()
                : null;

        return StudySetProgress.builder()
                .userId(userId)
                .studySetId(studySet.getId())
                .folderId(folderId)
                .packageId(packageId)
                .status(ProgressStatus.NOT_STARTED)
                .completedModules(0)
                .totalModules(0)
                .requiredCompletedModules(0)
                .totalRequiredModules(0)
                .build();
    }

    private ModuleProgressDto toDto(ModuleProgress progress) {
        return ModuleProgressDto.builder()
                .id(progress.getId())
                .studySetModuleId(progress.getStudySetModuleId())
                .studySetId(progress.getStudySetId())
                .status(progress.getStatus())
                .completedItems(progress.getCompletedItems())
                .totalItems(progress.getTotalItems())
                .progressPercentage(progress.getProgressPercentage())
                .score(progress.getScore())
                .studyTimeSeconds(progress.getStudyTimeSeconds())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}
