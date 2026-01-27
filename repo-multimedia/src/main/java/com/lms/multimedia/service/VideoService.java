package com.lms.multimedia.service;

import com.lms.content.common.entity.StudySet;
import com.lms.content.common.entity.Video;
import com.lms.content.common.entity.enums.VideoStatus;
import com.lms.multimedia.dto.request.VideoWebhookRequest;
import com.lms.multimedia.dto.response.VideoResponse;
import com.lms.multimedia.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final VideoRepository videoRepository;

    /**
     * Handle webhook from Express.js video streaming service
     */
    @Transactional
    public VideoResponse handleWebhook(VideoWebhookRequest request) {
        log.info("Received webhook for video code: {}", request.getCode());

        Video video = videoRepository.findByCode(request.getCode())
                .orElse(new Video());

        // Update video fields
        video.setCode(request.getCode());
        video.setName(request.getName());
        video.setDescription(request.getDescription());
        video.setUserId(request.getUserId());
        video.setStatus(VideoStatus.valueOf(request.getStatus().toUpperCase()));
        video.setDuration(request.getDuration());
        video.setThumbnailPath(request.getThumbnailPath());
        video.setKaraokePath(request.getKaraokePath());

        video = videoRepository.save(video);
        log.info("Video saved successfully: {}", video.getCode());

        return mapToResponse(video);
    }

    /**
     * Get all videos
     */
    public List<VideoResponse> getAllVideos() {
        return videoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get video by code
     */
    public VideoResponse getVideoByCode(String code) {
        Video video = videoRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Video not found with code: " + code));
        return mapToResponse(video);
    }

    /**
     * Get videos by user ID
     */
    public List<VideoResponse> getVideosByUserId(String userId) {
        return videoRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Video entity to VideoResponse DTO
     */
    private VideoResponse mapToResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .code(video.getCode())
                .name(video.getName())
                .description(video.getDescription())
                .userId(video.getUserId())
                .status(video.getStatus())
                .duration(video.getDuration())
                .thumbnailPath(video.getThumbnailPath())
                .karaokePath(video.getKaraokePath())
                .createdAt(video.getCreatedAt() != null ? video.getCreatedAt().toString() : null)
                .updatedAt(video.getUpdatedAt() != null ? video.getUpdatedAt().toString() : null)
                .build();
    }
}
