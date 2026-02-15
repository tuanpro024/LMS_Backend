package com.lms.listening.service.impl;

import com.lms.listening.exception.ResourceNotFoundException;
import com.lms.listening.dto.request.AddVideoRequest;
import com.lms.listening.dto.response.VideoMetadataResponse;
import com.lms.listening.entity.VideoMetadata;
import com.lms.listening.repository.VideoMetadataRepository;
import com.lms.listening.service.VideoMetadataService;
import com.lms.content.common.service.StudySetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoMetadataServiceImpl implements VideoMetadataService {

    private final VideoMetadataRepository videoMetadataRepository;
    private final StudySetService studySetService; // From content-common
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${listening.multimedia-service.url}")
    private String multimediaServiceUrl;

    @Override
    @Transactional
    public VideoMetadataResponse addVideoToStudySet(String studySetId, AddVideoRequest request, String userId) {
        log.info("Adding video {} to study set {}", request.getVideoCode(), studySetId);

        // Verify study set exists
        studySetService.getStudySetById(studySetId);

        // Check if video already exists in study set
        if (videoMetadataRepository.existsByStudySetIdAndVideoCodeAndDeletedFalse(studySetId, request.getVideoCode())) {
            throw new IllegalArgumentException("Video already exists in this study set");
        }

        // Fetch video metadata from multimedia service
        VideoMetadataFromMultimedia videoData = fetchVideoFromMultimedia(request.getVideoCode());

        // Create video metadata entity
        VideoMetadata videoMetadata = VideoMetadata.builder()
                .studySetId(studySetId)
                .videoCode(request.getVideoCode())
                .name(videoData.getName())
                .description(videoData.getDescription())
                .thumbnailPath(videoData.getThumbnailPath())
                .duration(videoData.getDuration())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .playlistUrl(videoData.getPlaylistUrl())
                .build();

        videoMetadata = videoMetadataRepository.save(videoMetadata);
        log.info("Added video {} to study set {} with ID {}", request.getVideoCode(), studySetId,
                videoMetadata.getId());

        return mapToResponse(videoMetadata);
    }

    @Override
    public List<VideoMetadataResponse> getVideosInStudySet(String studySetId) {
        log.info("Getting all videos in study set {}", studySetId);
        List<VideoMetadata> videos = videoMetadataRepository
                .findByStudySetIdAndDeletedFalseOrderByDisplayOrder(studySetId);
        return videos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeVideoFromStudySet(String studySetId, String videoCode, String userId) {
        log.info("Removing video {} from study set {}", videoCode, studySetId);

        VideoMetadata videoMetadata = videoMetadataRepository
                .findByStudySetIdAndVideoCodeAndDeletedFalse(studySetId, videoCode)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found in this study set"));

        videoMetadata.setDeleted(true);
        videoMetadataRepository.save(videoMetadata);

        log.info("Removed video {} from study set {}", videoCode, studySetId);
    }

    @Override
    @Transactional
    public VideoMetadataResponse updateVideoDisplayOrder(String studySetId, String videoCode, Integer displayOrder,
            String userId) {
        log.info("Updating display order of video {} in study set {} to {}", videoCode, studySetId, displayOrder);

        VideoMetadata videoMetadata = videoMetadataRepository
                .findByStudySetIdAndVideoCodeAndDeletedFalse(studySetId, videoCode)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found in this study set"));

        videoMetadata.setDisplayOrder(displayOrder);
        videoMetadata = videoMetadataRepository.save(videoMetadata);

        return mapToResponse(videoMetadata);
    }

    @Override
    @Transactional
    public VideoMetadataResponse refreshVideoMetadata(String studySetId, String videoCode) {
        log.info("Refreshing metadata for video {} in study set {}", videoCode, studySetId);

        VideoMetadata videoMetadata = videoMetadataRepository
                .findByStudySetIdAndVideoCodeAndDeletedFalse(studySetId, videoCode)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found in this study set"));

        // Fetch fresh metadata from multimedia service
        VideoMetadataFromMultimedia videoData = fetchVideoFromMultimedia(videoCode);

        // Update metadata
        videoMetadata.setName(videoData.getName());
        videoMetadata.setDescription(videoData.getDescription());
        videoMetadata.setThumbnailPath(videoData.getThumbnailPath());
        videoMetadata.setDuration(videoData.getDuration());
        videoMetadata.setPlaylistUrl(videoData.getPlaylistUrl());

        videoMetadata = videoMetadataRepository.save(videoMetadata);

        return mapToResponse(videoMetadata);
    }

    /**
     * Fetch video metadata from multimedia service
     */
    private VideoMetadataFromMultimedia fetchVideoFromMultimedia(String videoCode) {
        try {
            String url = multimediaServiceUrl + "/api/videos/" + videoCode;
            log.info("Fetching video metadata from: {}", url);

            // This assumes multimedia service returns a response with data field
            MultimediaApiResponse response = restTemplate.getForObject(url, MultimediaApiResponse.class);

            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Video not found in multimedia service: " + videoCode);
            }

            return response.getData();
        } catch (RestClientException e) {
            log.error("Error fetching video from multimedia service", e);
            throw new RuntimeException("Failed to fetch video from multimedia service: " + e.getMessage());
        }
    }

    /**
     * Map entity to response DTO
     */
    private VideoMetadataResponse mapToResponse(VideoMetadata video) {
        return VideoMetadataResponse.builder()
                .id(video.getId())
                .studySetId(video.getStudySetId())
                .videoCode(video.getVideoCode())
                .name(video.getName())
                .description(video.getDescription())
                .thumbnailPath(video.getThumbnailPath())
                .duration(video.getDuration())
                .displayOrder(video.getDisplayOrder())
                .playlistUrl(video.getPlaylistUrl())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    // Inner classes for deserializing multimedia service response
    private static class MultimediaApiResponse {
        private VideoMetadataFromMultimedia data;

        public VideoMetadataFromMultimedia getData() {
            return data;
        }

        public void setData(VideoMetadataFromMultimedia data) {
            this.data = data;
        }
    }

    private static class VideoMetadataFromMultimedia {
        private String code;
        private String name;
        private String description;
        private String thumbnailPath;
        private Integer duration;
        private String playlistUrl;

        // Getters and setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getThumbnailPath() {
            return thumbnailPath;
        }

        public void setThumbnailPath(String thumbnailPath) {
            this.thumbnailPath = thumbnailPath;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public String getPlaylistUrl() {
            return playlistUrl;
        }

        public void setPlaylistUrl(String playlistUrl) {
            this.playlistUrl = playlistUrl;
        }
    }
}
