package com.lms.multimedia.service;

import com.lms.multimedia.dto.request.SubtitleWebhookRequest;
import com.lms.multimedia.dto.response.SubtitleResponse;
import com.lms.multimedia.entity.Subtitle;
import com.lms.multimedia.entity.Video;
import com.lms.multimedia.entity.enums.SubtitleStatus;
import com.lms.multimedia.repository.SubtitleRepository;
import com.lms.multimedia.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtitleService {

    private final SubtitleRepository subtitleRepository;
    private final VideoRepository videoRepository;

    /**
     * Handle subtitle upload webhook from Express service
     */
    @Transactional
    public SubtitleResponse handleWebhook(SubtitleWebhookRequest request) {
        log.info("Handling subtitle webhook for video: {}", request.getVideoCode());

        // 1. Find video by code
        Video video = videoRepository.findByCode(request.getVideoCode())
                .orElseThrow(() -> new RuntimeException("Video not found with code: " + request.getVideoCode()));

        // 2. Create Subtitle entity
        Subtitle subtitle = Subtitle.builder()
                .videoId(video.getId())
                .videoCode(request.getVideoCode())
                .name(request.getSubtitleName())
                .filePath(request.getFilePath())
                .status(SubtitleStatus.INACTIVE)
                .build();

        subtitle = subtitleRepository.save(subtitle);

        // 3. Update video hasSubtitle flag
        if (!video.getHasSubtitle()) {
            video.setHasSubtitle(true);
            videoRepository.save(video);
        }

        log.info("Subtitle created successfully: {}", subtitle.getId());
        return mapToResponse(subtitle);
    }

    /**
     * Get all subtitles for a video (non-deleted)
     */
    public List<SubtitleResponse> getSubtitlesByVideoId(String videoId) {
        return subtitleRepository.findByVideoIdAndDeletedFalse(videoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get the active subtitle for a video (supports both videoId and videoCode)
     */
    public Optional<SubtitleResponse> getActiveSubtitle(String videoId) {
        // Try by videoId first (ULID), then by videoCode
        Optional<Subtitle> result = subtitleRepository.findByVideoIdAndStatusAndDeletedFalse(videoId,
                SubtitleStatus.ACTIVE);
        if (result.isEmpty()) {
            result = subtitleRepository.findByVideoCodeAndStatusAndDeletedFalse(videoId, SubtitleStatus.ACTIVE);
        }
        return result.map(this::mapToResponse);
    }

    /**
     * Set a subtitle as ACTIVE (and deactivate others)
     */
    @Transactional
    public SubtitleResponse setActiveSubtitle(String subtitleId) {
        log.info("Setting subtitle as active: {}", subtitleId);

        Subtitle subtitle = subtitleRepository.findById(subtitleId)
                .orElseThrow(() -> new RuntimeException("Subtitle not found with ID: " + subtitleId));

        if (subtitle.isDeleted()) {
            throw new RuntimeException("Cannot activate deleted subtitle");
        }

        // Deactivate all other subtitles for this video
        subtitleRepository.updateStatusByVideoIdExcept(
                subtitle.getVideoId(),
                subtitleId,
                SubtitleStatus.INACTIVE);

        // Activate this subtitle
        subtitle.setStatus(SubtitleStatus.ACTIVE);
        subtitle = subtitleRepository.save(subtitle);

        return mapToResponse(subtitle);
    }

    /**
     * Soft delete subtitle
     */
    @Transactional
    public void deleteSubtitle(String subtitleId) {
        log.info("Soft deleting subtitle: {}", subtitleId);

        Subtitle subtitle = subtitleRepository.findById(subtitleId)
                .orElseThrow(() -> new RuntimeException("Subtitle not found with ID: " + subtitleId));

        // Mark as deleted
        subtitle.setDeleted(true);
        subtitleRepository.save(subtitle);

        // If it was active, try to activate another subtitle
        if (subtitle.getStatus() == SubtitleStatus.ACTIVE) {
            Optional<Subtitle> nextSubtitle = subtitleRepository.findByVideoIdAndDeletedFalse(subtitle.getVideoId())
                    .stream()
                    .findFirst();

            nextSubtitle.ifPresent(sub -> {
                sub.setStatus(SubtitleStatus.ACTIVE);
                subtitleRepository.save(sub);
            });
        }

        // Update video hasSubtitle flag if no subtitles left
        long remainingCount = subtitleRepository.countByVideoIdAndDeletedFalse(subtitle.getVideoId());
        if (remainingCount == 0) {
            videoRepository.findById(subtitle.getVideoId()).ifPresent(video -> {
                video.setHasSubtitle(false);
                videoRepository.save(video);
            });
        }
    }

    /**
     * Map Subtitle entity to response DTO
     */
    private SubtitleResponse mapToResponse(Subtitle subtitle) {
        return SubtitleResponse.builder()
                .id(subtitle.getId())
                .videoId(subtitle.getVideoId())
                .videoCode(subtitle.getVideoCode())
                .name(subtitle.getName())
                .filePath(subtitle.getFilePath())
                .status(subtitle.getStatus())
                .createdAt(subtitle.getCreatedAt() != null ? subtitle.getCreatedAt().toString() : null)
                .updatedAt(subtitle.getUpdatedAt() != null ? subtitle.getUpdatedAt().toString() : null)
                .build();
    }
}
