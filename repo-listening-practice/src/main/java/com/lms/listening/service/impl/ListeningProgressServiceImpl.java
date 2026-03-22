package com.lms.listening.service.impl;

import com.lms.listening.dto.request.UpdateListeningProgressRequest;
import com.lms.listening.dto.response.ListeningStudySetProgressResponse;
import com.lms.listening.dto.response.ListeningVideoProgressResponse;
import com.lms.listening.entity.ListeningStudySetProgress;
import com.lms.listening.entity.ListeningVideoProgress;
import com.lms.listening.entity.VideoMetadata;
import com.lms.listening.entity.enums.ProgressStatus;
import com.lms.listening.exception.ResourceNotFoundException;
import com.lms.listening.repository.ListeningStudySetProgressRepository;
import com.lms.listening.repository.ListeningVideoProgressRepository;
import com.lms.listening.repository.VideoMetadataRepository;
import com.lms.listening.service.ListeningProgressService;
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
public class ListeningProgressServiceImpl implements ListeningProgressService {

    private final ListeningVideoProgressRepository videoProgressRepository;
    private final ListeningStudySetProgressRepository studySetProgressRepository;
    private final VideoMetadataRepository videoMetadataRepository;

    @Override
    @Transactional
    public ListeningVideoProgressResponse startWatching(String userId, String studySetId, String videoCode) {
        log.info("User {} started watching video {} in study set {}", userId, videoCode, studySetId);

        VideoMetadata video = validateVideoMetadata(studySetId, videoCode);

        ListeningVideoProgress progress = videoProgressRepository
                .findByUserIdAndStudySetIdAndVideoCode(userId, studySetId, videoCode)
                .orElseGet(() -> {
                    ListeningVideoProgress p = ListeningVideoProgress.builder()
                            .userId(userId)
                            .studySetId(studySetId)
                            .videoCode(videoCode)
                            .status(ProgressStatus.IN_PROGRESS)
                            .watchedSeconds(0)
                            .totalDurationSeconds(video.getDuration() != null ? video.getDuration() : 0)
                            .watchPercent(0.0)
                            .lastPositionSeconds(0)
                            .isCompleted(false)
                            .firstStartedAt(Instant.now())
                            .lastWatchedAt(Instant.now())
                            .build();
                    return videoProgressRepository.save(p);
                });

        return toVideoResponse(progress);
    }

    @Override
    @Transactional
    public ListeningVideoProgressResponse updateWatchProgress(String userId, String studySetId, String videoCode, UpdateListeningProgressRequest request) {
        VideoMetadata video = validateVideoMetadata(studySetId, videoCode);

        ListeningVideoProgress progress = videoProgressRepository
                .findByUserIdAndStudySetIdAndVideoCode(userId, studySetId, videoCode)
                .orElseGet(() -> ListeningVideoProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .videoCode(videoCode)
                        .status(ProgressStatus.IN_PROGRESS)
                        .watchedSeconds(0)
                        .totalDurationSeconds(video.getDuration() != null ? video.getDuration() : 0)
                        .watchPercent(0.0)
                        .lastPositionSeconds(0)
                        .isCompleted(false)
                        .firstStartedAt(Instant.now())
                        .build()
                );

        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            return toVideoResponse(progress);
        }

        if (video.getDuration() != null) {
            progress.setTotalDurationSeconds(video.getDuration());
        }

        if (progress.getTotalDurationSeconds() == 0 && request.getWatchedSeconds() > 0) {
            progress.setTotalDurationSeconds(request.getWatchedSeconds());
        }

        progress.updateWatchTime(request.getWatchedSeconds());

        if (request.getLastPositionSeconds() != null) {
            progress.setLastPositionSeconds(request.getLastPositionSeconds());
        }

        if (progress.qualifiesForCompletion()) {
            progress.markCompleted();
            progress = videoProgressRepository.save(progress);
            updateStudySetProgress(userId, studySetId);
        } else {
            progress = videoProgressRepository.save(progress);
        }

        return toVideoResponse(progress);
    }

    @Override
    @Transactional
    public ListeningVideoProgressResponse forceComplete(String userId, String studySetId, String videoCode) {
        VideoMetadata video = validateVideoMetadata(studySetId, videoCode);
        ListeningVideoProgress progress = videoProgressRepository
                .findByUserIdAndStudySetIdAndVideoCode(userId, studySetId, videoCode)
                .orElseGet(() -> ListeningVideoProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .videoCode(videoCode)
                        .status(ProgressStatus.IN_PROGRESS)
                        .watchedSeconds(0)
                        .totalDurationSeconds(video.getDuration() != null ? video.getDuration() : 0)
                        .watchPercent(1.0)
                        .lastPositionSeconds(0)
                        .isCompleted(false)
                        .firstStartedAt(Instant.now())
                        .build()
                );

        if (progress.getStatus() != ProgressStatus.COMPLETED) {
            progress.markCompleted();
            progress = videoProgressRepository.save(progress);
            updateStudySetProgress(userId, studySetId);
        }

        return toVideoResponse(progress);
    }

    @Override
    public ListeningVideoProgressResponse getVideoProgress(String userId, String studySetId, String videoCode) {
        return videoProgressRepository.findByUserIdAndStudySetIdAndVideoCode(userId, studySetId, videoCode)
                .map(this::toVideoResponse)
                .orElse(null);
    }

    @Override
    public ListeningStudySetProgressResponse getStudySetProgress(String userId, String studySetId) {
        return studySetProgressRepository.findByUserIdAndStudySetId(userId, studySetId)
                .map(this::toStudySetResponse)
                .orElse(null);
    }

    private void updateStudySetProgress(String userId, String studySetId) {
        List<VideoMetadata> allVideos = videoMetadataRepository.findByStudySetIdAndDeletedFalseOrderByDisplayOrder(studySetId);
        int totalVideos = allVideos.size();

        List<String> videoCodes = allVideos.stream().map(VideoMetadata::getVideoCode).collect(Collectors.toList());

        List<ListeningVideoProgress> allProgress = videoProgressRepository.findByUserIdAndStudySetId(userId, studySetId);
        long completedVideos = allProgress.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED && videoCodes.contains(p.getVideoCode()))
                .count();

        ProgressStatus status;
        if (completedVideos == 0) {
            status = ProgressStatus.NOT_STARTED;
        } else if (completedVideos >= totalVideos && totalVideos > 0) {
            status = ProgressStatus.COMPLETED;
        } else {
            status = ProgressStatus.IN_PROGRESS;
        }

        ListeningStudySetProgress progress = studySetProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElseGet(() -> ListeningStudySetProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .status(ProgressStatus.NOT_STARTED)
                        .completedVideos(0)
                        .totalVideos(totalVideos)
                        .progressPercentage(0.0)
                        .build());

        progress.setStatus(status);
        progress.setCompletedVideos((int) completedVideos);
        progress.setTotalVideos(totalVideos);
        progress.updateProgressPercent();

        if (progress.getFirstStartedAt() == null && completedVideos > 0) {
            progress.setFirstStartedAt(Instant.now());
        }
        if (status == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }

        studySetProgressRepository.save(progress);
    }

    private VideoMetadata validateVideoMetadata(String studySetId, String videoCode) {
        return videoMetadataRepository.findByStudySetIdAndVideoCodeAndDeletedFalse(studySetId, videoCode)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found in study set"));
    }

    private ListeningVideoProgressResponse toVideoResponse(ListeningVideoProgress p) {
        return ListeningVideoProgressResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .studySetId(p.getStudySetId())
                .videoCode(p.getVideoCode())
                .status(p.getStatus())
                .watchedSeconds(p.getWatchedSeconds())
                .totalDurationSeconds(p.getTotalDurationSeconds())
                .watchPercent(p.getWatchPercent())
                .lastPositionSeconds(p.getLastPositionSeconds())
                .isCompleted(p.getIsCompleted())
                .firstStartedAt(p.getFirstStartedAt())
                .lastWatchedAt(p.getLastWatchedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }

    private ListeningStudySetProgressResponse toStudySetResponse(ListeningStudySetProgress p) {
        return ListeningStudySetProgressResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .studySetId(p.getStudySetId())
                .status(p.getStatus())
                .completedVideos(p.getCompletedVideos())
                .totalVideos(p.getTotalVideos())
                .progressPercentage(p.getProgressPercentage())
                .firstStartedAt(p.getFirstStartedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }
}
