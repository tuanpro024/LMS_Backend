package com.lms.learningpath.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.learningpath.dto.request.CompleteModuleRequest;
import com.lms.learningpath.dto.request.CreateModuleRequest;
import com.lms.learningpath.dto.request.UpdateModuleRequest;
import com.lms.learningpath.dto.response.*;
import com.lms.learningpath.entity.LearningModule;
import com.lms.learningpath.entity.UserModuleProgress;
import com.lms.learningpath.entity.enums.EventType;
import com.lms.learningpath.entity.enums.ModuleStatus;
import com.lms.learningpath.mapper.LearningModuleMapper;
import com.lms.learningpath.repository.LearningModuleRepository;
import com.lms.learningpath.repository.UserModuleProgressRepository;
import com.lms.learningpath.service.*;
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
public class LearningModuleServiceImpl implements LearningModuleService {

    private final LearningModuleRepository moduleRepository;
    private final UserModuleProgressRepository progressRepository;
    private final LearningModuleMapper moduleMapper;
    private final ProgressTrackingService progressTrackingService;
    private final SetUnlockService setUnlockService;
    private final LearningEventService eventService;
    private final QuestService questService;

    @Override
    @Transactional
    public ModuleResponse createModule(CreateModuleRequest request, String userId) {
        log.info("Creating module for study set: {}", request.getStudySetId());

        // TODO: Verify study set exists and user has permission
        // (Call to content-common service via Feign)

        LearningModule module = moduleMapper.toEntity(request);
        LearningModule saved = moduleRepository.save(module);

        log.info("Created module: {} for study set: {}", saved.getId(), saved.getStudySetId());

        return moduleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleResponse getModuleById(String id) {
        LearningModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Module not found"));
        return moduleMapper.toResponse(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleResponse> getModulesByStudySetId(String studySetId, String userId) {
        List<LearningModule> modules = moduleRepository
                .findByStudySetIdOrderByOrderIndexAsc(studySetId);

        // Get user progress for each module
        return modules.stream()
                .map(module -> {
                    UserModuleProgress progress = progressRepository
                            .findByUserIdAndModuleId(userId, module.getId())
                            .orElse(null);
                    return moduleMapper.toResponseWithProgress(module, progress);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ModuleResponse updateModule(String id, UpdateModuleRequest request, String userId) {
        LearningModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Module not found"));

        // TODO: Check ownership via study set

        // Update fields
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }
        if (request.getType() != null) {
            module.setType(request.getType());
        }
        if (request.getTitle() != null) {
            module.setTitle(request.getTitle());
        }
        if (request.getSubtitle() != null) {
            module.setSubtitle(request.getSubtitle());
        }
        if (request.getIcon() != null) {
            module.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            module.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }
        if (request.getContentSetId() != null) {
            module.setContentSetId(request.getContentSetId());
        }
        if (request.getExternalRefJson() != null) {
            module.setExternalRefJson(request.getExternalRefJson());
        }
        if (request.getEstimatedMinutes() != null) {
            module.setEstimatedMinutes(request.getEstimatedMinutes());
        }
        if (request.getIsRequired() != null) {
            module.setIsRequired(request.getIsRequired());
        }

        LearningModule updated = moduleRepository.save(module);
        return moduleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteModule(String id, String userId) {
        LearningModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Module not found"));

        // TODO: Check ownership

        moduleRepository.delete(module);
        log.info("Deleted module: {}", id);
    }

    @Override
    @Transactional
    public ModuleCompleteResponse startModule(String moduleId, String userId) {
        LearningModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Module not found"));

        // Get or create progress
        UserModuleProgress progress = progressRepository
                .findByUserIdAndModuleId(userId, moduleId)
                .orElseGet(() -> {
                    UserModuleProgress newProgress = UserModuleProgress.builder()
                            .userId(userId)
                            .module(module)
                            .status(ModuleStatus.IN_PROGRESS)
                            .attempts(0)
                            .build();
                    return progressRepository.save(newProgress);
                });

        // Update status if not started
        if (progress.getStatus() == ModuleStatus.NOT_STARTED) {
            progress.setStatus(ModuleStatus.IN_PROGRESS);
            progressRepository.save(progress);
        }

        // Log event
        eventService.logSimpleEvent(userId, EventType.MODULE_STARTED, module.getStudySetId(), moduleId);

        return ModuleCompleteResponse.builder()
                .moduleId(moduleId)
                .status(progress.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public ModuleCompleteResponse completeModule(
            String moduleId,
            String userId,
            CompleteModuleRequest request
    ) {
        LearningModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Module not found"));

        // Update module progress
        UserModuleProgress progress = updateModuleProgress(userId, module, request);

        // Calculate exp
        Integer earnedExp = calculateExp(request.getScore());

        // Update set progress
        SetProgressResponse setProgress = progressTrackingService
                .updateSetProgress(userId, module.getStudySetId());

        // Check if set is completed
        boolean setCompleted = setProgress.getCanCompleteSet() &&
                !setProgress.getStatus().name().equals("COMPLETED");

        SetCompletionResult setResult = null;
        List<UnlockedSetInfo> unlockedSets = null;

        if (setCompleted) {
            // Complete the set
            setResult = progressTrackingService.completeSet(userId, module.getStudySetId());

            // Unlock next sets
            unlockedSets = setUnlockService.unlockNextSets(userId, module.getStudySetId());
        }

        // Update quests
        List<QuestProgressInfo> questsUpdated = questService.updateQuestProgress(
                userId,
                moduleId,
                module.getStudySetId()
        );

        // Get suggested next module
        ModuleResponse suggestedNext = getSuggestedNextModule(userId, module.getStudySetId());

        // Log event
        eventService.logEvent(
                userId,
                EventType.MODULE_COMPLETED,
                module.getStudySetId(),
                moduleId,
                request.getScore(),
                request.getDurationSeconds(),
                request.getMetadata()
        );

        return ModuleCompleteResponse.builder()
                .moduleId(moduleId)
                .status(ModuleStatus.COMPLETED.name())
                .score(request.getScore())
                .earnedExp(earnedExp)
                .completedAt(progress.getCompletedAt())
                .setProgress(setProgress)
                .suggestedNextModule(suggestedNext)
                .setCompleted(setCompleted)
                .setResult(setResult)
                .unlockedSets(unlockedSets)
                .questsUpdated(questsUpdated)
                .build();
    }

    private UserModuleProgress updateModuleProgress(
            String userId,
            LearningModule module,
            CompleteModuleRequest request
    ) {
        UserModuleProgress progress = progressRepository
                .findByUserIdAndModuleId(userId, module.getId())
                .orElseGet(() -> UserModuleProgress.builder()
                        .userId(userId)
                        .module(module)
                        .attempts(0)
                        .build());

        progress.setStatus(ModuleStatus.COMPLETED);
        progress.setScore(request.getScore());
        progress.setAttempts(progress.getAttempts() + 1);
        progress.setLastAttemptAt(Instant.now());
        progress.setDurationSeconds(request.getDurationSeconds());
        progress.setCompletedAt(Instant.now());
        progress.setMetadataJson(request.getMetadata());

        return progressRepository.save(progress);
    }

    private ModuleResponse getSuggestedNextModule(String userId, String studySetId) {
        List<ModuleResponse> modules = getModulesByStudySetId(studySetId, userId);

        // Find first NOT_STARTED or IN_PROGRESS module
        return modules.stream()
                .filter(m -> m.getStatus() == ModuleStatus.NOT_STARTED ||
                        m.getStatus() == ModuleStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);
    }

    private Integer calculateExp(Integer score) {
        // Simple exp calculation: score / 2
        return score / 2;
    }
}