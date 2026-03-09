package com.lms.videocourse.service.impl;

import com.lms.videocourse.client.MultimediaClient;
import com.lms.videocourse.client.dto.MultimediaVideoResponse;
import com.lms.videocourse.dto.request.CreateVideoModuleRequest;
import com.lms.videocourse.dto.request.UpdateVideoModuleRequest;
import com.lms.videocourse.dto.response.VideoModuleResponse;
import com.lms.videocourse.dto.response.VideoWatchProgressResponse;
import com.lms.videocourse.entity.VideoModule;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoModuleRepository;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.repository.VideoWatchProgressRepository;
import com.lms.videocourse.service.IVideoModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoModuleServiceImpl implements IVideoModuleService {

    private final VideoModuleRepository videoModuleRepository;
    private final VideoStepRepository videoStepRepository;
    private final VideoWatchProgressRepository watchProgressRepository;
    private final MultimediaClient multimediaClient;

    @Override
    @Transactional
    public VideoModuleResponse createVideoModule(CreateVideoModuleRequest request) {
        log.info("Creating video module: {} for step {}", request.getTitle(), request.getStepId());

        // Validate step exists
        videoStepRepository.findById(request.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + request.getStepId()));

        String videoUrl = request.getVideoUrl();
        String thumbnailUrl = request.getThumbnailUrl();
        Integer duration = request.getDuration();

        // If videoCode is provided, try to sync from repo-multimedia
        if (request.getVideoCode() != null && !request.getVideoCode().isBlank()) {
            try {
                MultimediaVideoResponse multimedia = multimediaClient.getVideoByCode(request.getVideoCode());
                if (multimedia != null) {
                    if (videoUrl == null && multimedia.getPlaylistUrl() != null) {
                        videoUrl = multimedia.getPlaylistUrl();
                    }
                    if (thumbnailUrl == null && multimedia.getThumbnailPath() != null) {
                        thumbnailUrl = multimedia.getThumbnailPath();
                    }
                    if (duration == null && multimedia.getDuration() != null) {
                        duration = multimedia.getDuration();
                    }
                    log.info("Auto-populated video fields from multimedia service for code: {}",
                            request.getVideoCode());
                }
            } catch (Exception e) {
                log.warn("Could not fetch from multimedia service for code {}: {}", request.getVideoCode(),
                        e.getMessage());
                // Continue with manually provided values
            }
        }

        VideoModule module = VideoModule.builder()
                .stepId(request.getStepId())
                .moduleOrder(request.getModuleOrder())
                .title(request.getTitle())
                .description(request.getDescription())
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .duration(duration)
                .subtitles(request.getSubtitles())
                .videoCode(request.getVideoCode())
                .moduleType(request.getModuleType())
                .contentSetId(request.getContentSetId())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .isActive(true)
                .build();

        module = videoModuleRepository.save(module);
        return toResponse(module, null);
    }

    @Override
    public VideoModuleResponse getVideoModuleById(String id, String userId) {
        VideoModule module = videoModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video module not found: " + id));
        VideoWatchProgressResponse watchProgress = getWatchProgressForUser(userId, id);
        return toResponse(module, watchProgress);
    }

    @Override
    public List<VideoModuleResponse> getVideoModulesByStepId(String stepId, String userId) {
        return videoModuleRepository.findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(stepId)
                .stream()
                .map(module -> {
                    VideoWatchProgressResponse watchProgress = getWatchProgressForUser(userId, module.getId());
                    return toResponse(module, watchProgress);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VideoModuleResponse updateVideoModule(String id, UpdateVideoModuleRequest request) {
        VideoModule module = videoModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video module not found: " + id));

        if (request.getTitle() != null)
            module.setTitle(request.getTitle());
        if (request.getDescription() != null)
            module.setDescription(request.getDescription());
        if (request.getModuleOrder() != null)
            module.setModuleOrder(request.getModuleOrder());
        if (request.getVideoUrl() != null)
            module.setVideoUrl(request.getVideoUrl());
        if (request.getThumbnailUrl() != null)
            module.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getDuration() != null)
            module.setDuration(request.getDuration());
        if (request.getSubtitles() != null)
            module.setSubtitles(request.getSubtitles());
        if (request.getVideoCode() != null)
            module.setVideoCode(request.getVideoCode());
        if (request.getModuleType() != null)
            module.setModuleType(request.getModuleType());
        if (request.getContentSetId() != null)
            module.setContentSetId(request.getContentSetId());
        if (request.getIsRequired() != null)
            module.setIsRequired(request.getIsRequired());
        if (request.getIsActive() != null)
            module.setIsActive(request.getIsActive());

        module = videoModuleRepository.save(module);
        return toResponse(module, null);
    }

    @Override
    @Transactional
    public void deleteVideoModule(String id) {
        VideoModule module = videoModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video module not found: " + id));
        module.setIsActive(false);
        videoModuleRepository.save(module);
    }

    private VideoWatchProgressResponse getWatchProgressForUser(String userId, String moduleId) {
        if (userId == null)
            return null;
        return watchProgressRepository.findByUserIdAndVideoModuleId(userId, moduleId)
                .map(p -> VideoWatchProgressResponse.builder()
                        .id(p.getId())
                        .videoModuleId(p.getVideoModuleId())
                        .stepId(p.getStepId())
                        .status(p.getStatus())
                        .watchedSeconds(p.getWatchedSeconds())
                        .totalDurationSeconds(p.getTotalDurationSeconds())
                        .watchPercent(p.getWatchPercent())
                        .lastPositionSeconds(p.getLastPositionSeconds())
                        .autoCompleted(p.getAutoCompleted())
                        .firstStartedAt(p.getFirstStartedAt())
                        .lastWatchedAt(p.getLastWatchedAt())
                        .completedAt(p.getCompletedAt())
                        .build())
                .orElse(null);
    }

    private VideoModuleResponse toResponse(VideoModule module, VideoWatchProgressResponse watchProgress) {
        return VideoModuleResponse.builder()
                .id(module.getId())
                .stepId(module.getStepId())
                .moduleOrder(module.getModuleOrder())
                .title(module.getTitle())
                .description(module.getDescription())
                .videoUrl(module.getVideoUrl())
                .thumbnailUrl(module.getThumbnailUrl())
                .duration(module.getDuration())
                .subtitles(module.getSubtitles())
                .videoCode(module.getVideoCode())
                .moduleType(module.getModuleType())
                .contentSetId(module.getContentSetId())
                .isRequired(module.getIsRequired())
                .isActive(module.getIsActive())
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .watchProgress(watchProgress)
                .build();
    }
}
