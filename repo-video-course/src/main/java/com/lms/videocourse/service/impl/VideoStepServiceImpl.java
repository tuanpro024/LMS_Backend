package com.lms.videocourse.service.impl;

import com.lms.videocourse.dto.request.CreateVideoStepRequest;
import com.lms.videocourse.dto.request.UpdateVideoStepRequest;
import com.lms.videocourse.dto.response.VideoStepProgressResponse;
import com.lms.videocourse.dto.response.VideoStepResponse;
import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.repository.VideoModuleRepository;
import com.lms.videocourse.service.IVideoStepService;
import com.lms.videocourse.service.IVideoUnlockService;
import com.lms.videocourse.service.IVideoProgressService;
import com.lms.common.util.SanitizationUtils;
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
    private final VideoModuleRepository videoModuleRepository;
    private final IVideoUnlockService unlockService;
    private final IVideoProgressService progressService;

    @Override
    @Transactional
    public VideoStepResponse createVideoStep(CreateVideoStepRequest request) {
        log.info("Creating video step: {} for studySet {}", request.getTitle(), request.getStudySetId());

        // Validate studySet exists (using videoCourseRepository for now if it's the source, but we
        // should ideally check studySet)
        // Since we are removing VideoCourse, we bypass the course check.
        // We'll trust the studySetId provided for now or implement a different check.

        VideoStep step = VideoStep.builder()
                .studySetId(request.getStudySetId())
                .title(SanitizationUtils.stripHtmlTags(request.getTitle()))
                .description(SanitizationUtils.stripHtmlTags(request.getDescription()))
                .stepOrder(request.getStepOrder())
                .icon(request.getIcon())
                .color(request.getColor())
                .estimatedMinutes(request.getEstimatedMinutes())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();

        step = videoStepRepository.save(step);

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
    public List<VideoStepResponse> getVideoStepsByCourseId(String studySetId, String userId) {
        List<VideoStep> steps = videoStepRepository
                .findByStudySetIdAndIsActiveTrueOrderByStepOrderAsc(studySetId);

        return steps.stream().map(step -> {
            Boolean isUnlocked = null;
            String lockReason = null;
            VideoStepProgressResponse progress = null;

            if (userId != null) {
                isUnlocked = unlockService.isStepUnlocked(userId, step.getId());
                if (!isUnlocked) {
                    lockReason = unlockService.getLockReason(userId, step.getId());
                }
                progress = progressService.getStepProgress(userId, step.getId());
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
            step.setTitle(SanitizationUtils.stripHtmlTags(request.getTitle()));
        if (request.getDescription() != null)
            step.setDescription(SanitizationUtils.stripHtmlTags(request.getDescription()));
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
                .studySetId(step.getStudySetId())
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
