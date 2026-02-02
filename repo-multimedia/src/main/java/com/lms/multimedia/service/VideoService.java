package com.lms.multimedia.service;

import com.lms.multimedia.entity.Video;
import com.lms.multimedia.entity.enums.VideoStatus;
import com.lms.multimedia.dto.request.VideoWebhookRequest;
import com.lms.multimedia.dto.response.VideoResponse;
import com.lms.multimedia.dto.response.VideoDetailResponse;
import com.lms.multimedia.dto.response.SubtitleResponse;
import com.lms.multimedia.repository.VideoRepository;
import com.lms.multimedia.repository.SubtitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final VideoRepository videoRepository;
    private final SubtitleRepository subtitleRepository;

    /**
     * Handle webhook from Express.js video streaming service
     */
    @Transactional
    public VideoResponse handleWebhook(VideoWebhookRequest request) {
        log.info("Received webhook for video code: {}", request.getCode());

        Video video = videoRepository.findByCode(request.getCode())
                .orElse(new Video());

        // Update video fields (no userId tracking)
        video.setCode(request.getCode());
        video.setName(request.getName());
        video.setDescription(request.getDescription());
        video.setStatus(VideoStatus.valueOf(request.getStatus().toUpperCase()));
        video.setDuration(request.getDuration());
        video.setThumbnailPath(request.getThumbnailPath());
        video.setKaraokePath(request.getKaraokePath());

        // Set studySetId only if provided (optional field)
        if (request.getStudySetId() != null && !request.getStudySetId().isEmpty()) {
            video.setStudySetId(request.getStudySetId());
        }
        // Note: subtitlePath from webhook is ignored - we manage subtitles separately
        // in subtitle table

        video = videoRepository.save(video);
        log.info("Video saved successfully: {}", video.getCode());

        return mapToResponse(video);
    }

    /**
     * Get all videos (non-deleted by default)
     * 
     * @param status         Optional status filter
     * @param studySetId     Optional studySetId filter
     * @param includeDeleted Whether to include deleted videos (admin only)
     */
    public List<VideoResponse> getAllVideos(VideoStatus status, String studySetId, Boolean includeDeleted) {
        List<Video> videos;

        // Build query based on filters
        if (includeDeleted != null && includeDeleted) {
            // Admin: include deleted videos
            videos = videoRepository.findAll();
        } else {
            // Default: exclude deleted
            videos = videoRepository.findByDeletedFalse();
        }

        // Apply filters
        return videos.stream()
                .filter(v -> status == null || v.getStatus().equals(status))
                .filter(v -> studySetId == null || studySetId.equals(v.getStudySetId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all videos (legacy - excludes deleted)
     */
    public List<VideoResponse> getAllVideos() {
        return getAllVideos(null, null, false);
    }

    /**
     * Get video by code (excludes deleted)
     */
    public VideoResponse getVideoByCode(String code) {
        Video video = videoRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Video not found with code: " + code));
        return mapToResponse(video);
    }

    /**
     * Get detailed video information with subtitles
     * 
     * @param id Video ID
     * @return VideoDetailResponse with subtitles list
     */
    public VideoDetailResponse getVideoDetails(String id) {
        Video video = videoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));

        // Get all non-deleted subtitles for this video
        List<SubtitleResponse> subtitles = subtitleRepository.findByVideoCodeAndDeletedFalse(video.getCode())
                .stream()
                .map(this::mapSubtitleToResponse)
                .collect(Collectors.toList());

        return VideoDetailResponse.builder()
                .id(video.getId())
                .code(video.getCode())
                .name(video.getName())
                .description(video.getDescription())
                .status(video.getStatus())
                .duration(video.getDuration())
                .thumbnailPath(video.getThumbnailPath())
                .karaokePath(video.getKaraokePath())
                .studySetId(video.getStudySetId())
                .hasSubtitle(video.getHasSubtitle())
                .subtitles(subtitles)
                .createdAt(video.getCreatedAt() != null ? video.getCreatedAt().toString() : null)
                .updatedAt(video.getUpdatedAt() != null ? video.getUpdatedAt().toString() : null)
                .build();
    }

    /**
     * Soft delete video
     * 
     * @param id Video ID
     */
    @Transactional
    public void softDeleteVideo(String id) {
        Video video = videoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));

        video.setDeleted(true);
        videoRepository.save(video);
        log.info("Video soft deleted: {}", video.getCode());
    }

    /**
     * Get videos by user ID
     */

    /**
     * Get videos by study set ID
     */
    public List<VideoResponse> getVideosByStudySetId(String studySetId) {
        return videoRepository.findByStudySetId(studySetId).stream()
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
                .status(video.getStatus())
                .duration(video.getDuration())
                .thumbnailPath(video.getThumbnailPath())
                .karaokePath(video.getKaraokePath())
                .studySetId(video.getStudySetId())
                .createdAt(video.getCreatedAt() != null ? video.getCreatedAt().toString() : null)
                .updatedAt(video.getUpdatedAt() != null ? video.getUpdatedAt().toString() : null)
                .build();
    }

    /**
     * Map Subtitle entity to SubtitleResponse DTO
     */
    private SubtitleResponse mapSubtitleToResponse(com.lms.multimedia.entity.Subtitle subtitle) {
        return SubtitleResponse.builder()
                .id(subtitle.getId())
                .videoCode(subtitle.getVideoCode())
                .name(subtitle.getName())
                .filePath(subtitle.getFilePath())
                .status(subtitle.getStatus())
                .createdAt(subtitle.getCreatedAt() != null ? subtitle.getCreatedAt().toString() : null)
                .updatedAt(subtitle.getUpdatedAt() != null ? subtitle.getUpdatedAt().toString() : null)
                .build();
    }
}
