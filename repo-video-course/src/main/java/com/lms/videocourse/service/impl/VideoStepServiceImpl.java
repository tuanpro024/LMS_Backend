package com.lms.videocourse.service.impl;

import com.lms.videocourse.dto.request.CreateVideoStepRequest;
import com.lms.videocourse.dto.request.UpdateVideoStepRequest;
import com.lms.videocourse.dto.response.VideoStepProgressResponse;
import com.lms.videocourse.dto.response.VideoStepResponse;
import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.entity.VideoStepUnlockRule;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.repository.VideoStepProgressRepository;
import com.lms.videocourse.repository.VideoStepUnlockRuleRepository;
import com.lms.videocourse.repository.VideoCourseRepository;
import com.lms.videocourse.repository.VideoModuleRepository;
import com.lms.videocourse.service.IVideoStepService;
import com.lms.videocourse.service.IVideoUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoStepServiceImpl implements IVideoStepService {

    private final VideoStepRepository videoStepRepository;
    private final VideoStepProgressRepository stepProgressRepository;
    private final VideoStepUnlockRuleRepository unlockRuleRepository;
    private final VideoCourseRepository videoCourseRepository;
    private final VideoModuleRepository videoModuleRepository;
    private final IVideoUnlockService unlockService;

    @Override
    @Transactional
    public VideoStepResponse createVideoStep(CreateVideoStepRequest request) {
        log.info("Creating video step: {} for course {}", request.getTitle(), request.getVideoCourseId());

        // Validate course exists
        videoCourseRepository.findById(request.getVideoCourseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Video course not found: " + request.getVideoCourseId()));

        VideoStep step = VideoStep.builder()
                .videoCourseId(request.getVideoCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .stepOrder(request.getStepOrder())
                .icon(request.getIcon())
                .color(request.getColor())
                .estimatedMinutes(request.getEstimatedMinutes())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();

        step = videoStepRepository.save(step);

        // Auto-create sequential unlock rule (requires previous step)
        if (request.getStepOrder() > 1) {
            List<VideoStep> steps = videoStepRepository
                    .findByVideoCourseIdAndIsActiveTrueOrderByStepOrderAsc(request.getVideoCourseId());
            VideoStep previousStep = steps.stream()
                    .filter(s -> s.getStepOrder() == request.getStepOrder() - 1)
                    .findFirst()
                    .orElse(null);

            if (previousStep != null) {
                VideoStepUnlockRule rule = VideoStepUnlockRule.builder()
                        .stepId(step.getId())
                        .requiredStepId(previousStep.getId())
                        .requireAllModules(true)
                        .minimumWatchPercent(80)
                        .isActive(true)
                        .build();
                unlockRuleRepository.save(rule);
            }
        }

        int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
        return toResponse(step, null, null, null, moduleCount);
    }

    @Override
    public VideoStepResponse getVideoStepById(String id) {
        VideoStep step = videoStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + id));
        int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
        return toResponse(step, null, null, null, moduleCount);
    }

    @Override
    public List<VideoStepResponse> getVideoStepsByCourseId(String videoCourseId, String userId) {
        List<VideoStep> steps = videoStepRepository
                .findByVideoCourseIdAndIsActiveTrueOrderByStepOrderAsc(videoCourseId);

        return steps.stream().map(step -> {
            Boolean isUnlocked = null;
            String lockReason = null;
            VideoStepProgressResponse progress = null;

            if (userId != null) {
                isUnlocked = unlockService.isStepUnlocked(userId, step.getId());
                if (!isUnlocked) {
                    lockReason = unlockService.getLockReason(userId, step.getId());
                }
                progress = stepProgressRepository.findByUserIdAndStepId(userId, step.getId())
                        .map(p -> VideoStepProgressResponse.builder()
                                .id(p.getId())
                                .stepId(p.getStepId())
                                .videoCourseId(p.getVideoCourseId())
                                .status(p.getStatus())
                                .completedModules(p.getCompletedModules())
                                .totalModules(p.getTotalModules())
                                .requiredCompletedModules(p.getRequiredCompletedModules())
                                .totalRequiredModules(p.getTotalRequiredModules())
                                .progressPercentage(p.getProgressPercentage())
                                .firstStartedAt(p.getFirstStartedAt())
                                .completedAt(p.getCompletedAt())
                                .build())
                        .orElse(null);
            }
            int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
            return toResponse(step, isUnlocked, lockReason, progress, moduleCount);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VideoStepResponse updateVideoStep(String id, UpdateVideoStepRequest request) {
        VideoStep step = videoStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + id));

        if (request.getTitle() != null)
            step.setTitle(request.getTitle());
        if (request.getDescription() != null)
            step.setDescription(request.getDescription());
        if (request.getStepOrder() != null)
            step.setStepOrder(request.getStepOrder());
        if (request.getIcon() != null)
            step.setIcon(request.getIcon());
        if (request.getColor() != null)
            step.setColor(request.getColor());
        if (request.getEstimatedMinutes() != null)
            step.setEstimatedMinutes(request.getEstimatedMinutes());
        if (request.getIsRequired() != null)
            step.setIsRequired(request.getIsRequired());
        if (request.getIsActive() != null)
            step.setIsActive(request.getIsActive());

        step = videoStepRepository.save(step);
        int moduleCount = (int) videoModuleRepository.countByStepIdAndIsActiveTrue(step.getId());
        return toResponse(step, null, null, null, moduleCount);
    }

    @Override
    @Transactional
    public void deleteVideoStep(String id) {
        VideoStep step = videoStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + id));
        step.setIsActive(false);
        videoStepRepository.save(step);
    }

    private VideoStepResponse toResponse(VideoStep step, Boolean isUnlocked, String lockReason,
            VideoStepProgressResponse progress, int moduleCount) {
        return VideoStepResponse.builder()
                .id(step.getId())
                .videoCourseId(step.getVideoCourseId())
                .title(step.getTitle())
                .description(step.getDescription())
                .stepOrder(step.getStepOrder())
                .icon(step.getIcon())
                .color(step.getColor())
                .estimatedMinutes(step.getEstimatedMinutes())
                .isRequired(step.getIsRequired())
                .isActive(step.getIsActive())
                .createdAt(step.getCreatedAt())
                .updatedAt(step.getUpdatedAt())
                .isUnlocked(isUnlocked)
                .lockReason(lockReason)
                .moduleCount(moduleCount)
                .progress(progress)
                .build();
    }
}
