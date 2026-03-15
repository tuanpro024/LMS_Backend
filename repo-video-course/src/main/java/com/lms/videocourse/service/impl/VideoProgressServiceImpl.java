package com.lms.videocourse.service.impl;

import com.lms.videocourse.dto.request.CompleteWatchRequest;
import com.lms.videocourse.dto.request.UpdateWatchProgressRequest;
import com.lms.videocourse.dto.response.VideoCourseProgressResponse;
import com.lms.videocourse.dto.response.VideoStepProgressResponse;
import com.lms.videocourse.dto.response.VideoWatchProgressResponse;
import com.lms.videocourse.entity.*;
import com.lms.videocourse.entity.enums.ProgressStatus;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.*;
import com.lms.videocourse.service.IVideoProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core progress tracking service for repo-video-course.
 *
 * AUTO-COMPLETE RULE:
 * When watchedSeconds / duration >= 0.80 (80%), the VideoWatchProgress is
 * automatically
 * marked COMPLETED. This then triggers a rollup to VideoStepProgress and
 * VideoCourseProgress.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProgressServiceImpl implements IVideoProgressService {

        private final VideoModuleRepository videoModuleRepository;
        private final VideoWatchProgressRepository watchProgressRepository;
        private final VideoStepProgressRepository stepProgressRepository;
        private final VideoCourseProgressRepository courseProgressRepository;
        private final VideoStepRepository videoStepRepository;

        // ============ Watch Progress ============

        @Override
        @Transactional
        public VideoWatchProgressResponse startWatching(String userId, String moduleId) {
                log.info("User {} starting watch on module {}", userId, moduleId);

                VideoModule module = videoModuleRepository.findById(moduleId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Video module not found: " + moduleId));

                VideoWatchProgress progress = watchProgressRepository
                                .findByUserIdAndVideoModuleId(userId, moduleId)
                                .orElseGet(() -> {
                                        VideoWatchProgress newProgress = VideoWatchProgress.builder()
                                                        .userId(userId)
                                                        .videoModuleId(moduleId)
                                                        .stepId(module.getStepId())
                                                        .status(ProgressStatus.IN_PROGRESS)
                                                        .watchedSeconds(0)
                                                        .totalDurationSeconds(module.getDuration() != null
                                                                        ? module.getDuration()
                                                                        : 0)
                                                        .watchPercent(0.0)
                                                        .lastPositionSeconds(0)
                                                        .autoCompleted(false)
                                                        .firstStartedAt(Instant.now())
                                                        .lastWatchedAt(Instant.now())
                                                        .build();
                                        return watchProgressRepository.save(newProgress);
                                });

                return toWatchProgressResponse(progress);
        }

        @Override
        @Transactional
        public VideoWatchProgressResponse updateWatchProgress(String userId, String moduleId,
                        UpdateWatchProgressRequest request) {
                log.info("Updating watch progress for user {} on module {} — watchedSeconds={}", userId, moduleId,
                                request.getWatchedSeconds());

                VideoModule module = videoModuleRepository.findById(moduleId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Video module not found: " + moduleId));

                VideoWatchProgress progress = watchProgressRepository
                                .findByUserIdAndVideoModuleId(userId, moduleId)
                                .orElseGet(() -> VideoWatchProgress.builder()
                                                .userId(userId)
                                                .videoModuleId(moduleId)
                                                .stepId(module.getStepId())
                                                .status(ProgressStatus.IN_PROGRESS)
                                                .totalDurationSeconds(
                                                                module.getDuration() != null ? module.getDuration() : 0)
                                                .watchedSeconds(0)
                                                .watchPercent(0.0)
                                                .lastPositionSeconds(0)
                                                .autoCompleted(false)
                                                .firstStartedAt(Instant.now())
                                                .build());

                // Don't regress if already completed
                if (progress.getStatus() == ProgressStatus.COMPLETED) {
                        return toWatchProgressResponse(progress);
                }

                // Sync totalDurationSeconds from module in case it was updated
                if (module.getDuration() != null) {
                        progress.setTotalDurationSeconds(module.getDuration());
                }

                // Fallback: if totalDurationSeconds is still 0, use watchedSeconds as estimate
                // This handles cases where module.duration was never set
                if (progress.getTotalDurationSeconds() == 0 && request.getWatchedSeconds() > 0) {
                        progress.setTotalDurationSeconds(request.getWatchedSeconds());
                        log.info("Using watchedSeconds ({}) as fallback totalDurationSeconds for module {}",
                                        request.getWatchedSeconds(), moduleId);
                }

                // Update watch time (only moves forward, never backward)
                progress.updateWatchTime(request.getWatchedSeconds());

                // Update resume position
                if (request.getLastPositionSeconds() != null) {
                        progress.setLastPositionSeconds(request.getLastPositionSeconds());
                }

                // === AUTO-COMPLETE at 80% watch time ===
                if (progress.qualifiesForAutoComplete()) {
                        log.info("Auto-completing module {} for user {} at {}% watch time",
                                        moduleId, userId, String.format("%.1f", progress.getWatchPercent()));
                        progress.autoComplete();

                        progress = watchProgressRepository.save(progress);

                        // Trigger rollup to VideoStepProgress
                        updateStepProgress(userId, module.getStepId());

                        // Trigger rollup to VideoCourseProgress
                        VideoStep step = videoStepRepository.findById(module.getStepId())
                                        .orElseThrow();
                        updateCourseProgress(userId, step.getStudySetId());
                } else {
                        progress = watchProgressRepository.save(progress);
                }

                return toWatchProgressResponse(progress);
        }

        @Override
        @Transactional
        public VideoWatchProgressResponse completeWatch(String userId, String moduleId, CompleteWatchRequest request) {
                log.info("Force-completing module {} for user {}", moduleId, userId);

                VideoModule module = videoModuleRepository.findById(moduleId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Video module not found: " + moduleId));

                VideoWatchProgress progress = watchProgressRepository
                                .findByUserIdAndVideoModuleId(userId, moduleId)
                                .orElseGet(() -> VideoWatchProgress.builder()
                                                .userId(userId)
                                                .videoModuleId(moduleId)
                                                .stepId(module.getStepId())
                                                .status(ProgressStatus.NOT_STARTED)
                                                .totalDurationSeconds(
                                                                module.getDuration() != null ? module.getDuration() : 0)
                                                .watchedSeconds(0)
                                                .watchPercent(0.0)
                                                .lastPositionSeconds(0)
                                                .autoCompleted(false)
                                                .firstStartedAt(Instant.now())
                                                .build());

                if (progress.getStatus() != ProgressStatus.COMPLETED) {
                        progress.setStatus(ProgressStatus.COMPLETED);
                        progress.setAutoCompleted(false); // Manually completed
                        progress.setCompletedAt(Instant.now());
                        progress.setLastWatchedAt(Instant.now());
                        progress = watchProgressRepository.save(progress);

                        // Rollup
                        updateStepProgress(userId, module.getStepId());
                        VideoStep step = videoStepRepository.findById(module.getStepId()).orElseThrow();
                        updateCourseProgress(userId, step.getStudySetId());
                }

                return toWatchProgressResponse(progress);
        }

        @Override
        public VideoWatchProgressResponse getWatchProgress(String userId, String moduleId) {
                return watchProgressRepository.findByUserIdAndVideoModuleId(userId, moduleId)
                                .map(this::toWatchProgressResponse)
                                .orElse(null);
        }

        @Override
        public VideoStepProgressResponse getStepProgress(String userId, String stepId) {
                return stepProgressRepository.findByUserIdAndStepId(userId, stepId)
                                .map(this::toStepProgressResponse)
                                .orElse(null);
        }

        @Override
        public VideoCourseProgressResponse getCourseProgress(String userId, String studySetId) {
                return courseProgressRepository.findByUserIdAndStudySetId(userId, studySetId)
                                .map(this::toCourseProgressResponse)
                                .orElse(null);
        }

        @Override
        public List<VideoCourseProgressResponse> getAllCourseProgress(String userId, String studySetId) {
                // Return the progress for the study set itself
                return courseProgressRepository.findByUserIdAndStudySetId(userId, studySetId)
                                .map(p -> List.of(toCourseProgressResponse(p)))
                                .orElse(List.of());
        }

        // ============ Private rollup helpers ============

        /**
         * Recalculates VideoStepProgress from all VideoWatchProgress records in the
         * step.
         */
        private void updateStepProgress(String userId, String stepId) {
                VideoStep step = videoStepRepository.findById(stepId)
                                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + stepId));

                List<VideoModule> allModules = videoModuleRepository
                                .findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(stepId);

                int totalModules = allModules.size();
                long totalRequired = allModules.stream().filter(VideoModule::getIsRequired).count();

                List<String> moduleIds = allModules.stream()
                                .map(VideoModule::getId)
                                .collect(Collectors.toList());

                List<VideoWatchProgress> completedProgress = watchProgressRepository
                                .findByUserIdAndVideoModuleIdIn(userId, moduleIds)
                                .stream()
                                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                                .collect(Collectors.toList());

                int completedCount = completedProgress.size();
                long completedRequiredCount = completedProgress.stream()
                                .filter(wp -> {
                                        VideoModule vm = videoModuleRepository.findById(wp.getVideoModuleId())
                                                        .orElse(null);
                                        return vm != null && vm.getIsRequired();
                                }).count();

                ProgressStatus status;
                if (completedCount == 0) {
                        status = ProgressStatus.NOT_STARTED;
                } else if (completedRequiredCount >= totalRequired && totalRequired > 0) {
                        status = ProgressStatus.COMPLETED;
                } else {
                        status = ProgressStatus.IN_PROGRESS;
                }

                VideoStepProgress progress = stepProgressRepository
                                .findByUserIdAndStepId(userId, stepId)
                                .orElseGet(() -> VideoStepProgress.builder()
                                                .userId(userId)
                                                .stepId(stepId)
                                                .studySetId(step.getStudySetId())
                                                .status(ProgressStatus.NOT_STARTED)
                                                .completedModules(0)
                                                .totalModules(totalModules)
                                                .requiredCompletedModules(0)
                                                .totalRequiredModules((int) totalRequired)
                                                .build());

                progress.setStatus(status);
                progress.setCompletedModules(completedCount);
                progress.setTotalModules(totalModules);
                progress.setRequiredCompletedModules((int) completedRequiredCount);
                progress.setTotalRequiredModules((int) totalRequired);

                if (progress.getFirstStartedAt() == null && completedCount > 0) {
                        progress.setFirstStartedAt(Instant.now());
                }
                if (status == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
                        progress.setCompletedAt(Instant.now());
                }

                stepProgressRepository.save(progress);
                log.info("Updated step progress for user {} step {}: {}/{} required modules, status={}",
                                userId, stepId, completedRequiredCount, totalRequired, status);
        }

        /**
         * Recalculates VideoCourseProgress from all VideoStepProgress records in the
         * course.
         */
        private void updateCourseProgress(String userId, String studySetId) {
                // VideoCourse is being removed, but we still have studySetId to track overall
                // progress.

                List<VideoStep> allSteps = videoStepRepository
                                .findByStudySetIdAndIsActiveTrueOrderByStepOrderAsc(studySetId);

                int totalSteps = allSteps.size();

                List<String> stepIds = allSteps.stream()
                                .map(VideoStep::getId)
                                .collect(Collectors.toList());

                long completedSteps = stepProgressRepository.findByUserIdAndStepIdIn(userId, stepIds)
                                .stream()
                                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                                .count();

                ProgressStatus status;
                if (completedSteps == 0) {
                        status = ProgressStatus.NOT_STARTED;
                } else if (completedSteps >= totalSteps) {
                        status = ProgressStatus.COMPLETED;
                } else {
                        status = ProgressStatus.IN_PROGRESS;
                }

                // Find current step (first incomplete required step)
                String currentStepId = allSteps.stream()
                                .filter(s -> stepProgressRepository.findByUserIdAndStepId(userId, s.getId())
                                                .map(p -> p.getStatus() != ProgressStatus.COMPLETED)
                                                .orElse(true))
                                .findFirst()
                                .map(VideoStep::getId)
                                .orElse(null);

                VideoCourseProgress progress = courseProgressRepository
                                .findByUserIdAndStudySetId(userId, studySetId)
                                .orElseGet(() -> VideoCourseProgress.builder()
                                                .userId(userId)
                                                .studySetId(studySetId)
                                                .status(ProgressStatus.NOT_STARTED)
                                                .completedSteps(0)
                                                .totalSteps(totalSteps)
                                                .build());

                progress.setStatus(status);
                progress.setCompletedSteps((int) completedSteps);
                progress.setTotalSteps(totalSteps);
                progress.setCurrentStepId(currentStepId);

                if (progress.getFirstStartedAt() == null && completedSteps > 0) {
                        progress.setFirstStartedAt(Instant.now());
                }
                if (status == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
                        progress.setCompletedAt(Instant.now());
                }

                courseProgressRepository.save(progress);
                log.info("Updated course progress for user {} studySet {}: {}/{} steps, status={}",
                                userId, studySetId, completedSteps, totalSteps, status);
        }

        // ============ Mapping helpers ============

        private VideoWatchProgressResponse toWatchProgressResponse(VideoWatchProgress p) {
                return VideoWatchProgressResponse.builder()
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
                                .build();
        }

        private VideoStepProgressResponse toStepProgressResponse(VideoStepProgress p) {
                return VideoStepProgressResponse.builder()
                                .id(p.getId())
                                .stepId(p.getStepId())
                                .studySetId(p.getStudySetId())
                                .status(p.getStatus())
                                .completedModules(p.getCompletedModules())
                                .totalModules(p.getTotalModules())
                                .requiredCompletedModules(p.getRequiredCompletedModules())
                                .totalRequiredModules(p.getTotalRequiredModules())
                                .progressPercentage(p.getProgressPercentage())
                                .firstStartedAt(p.getFirstStartedAt())
                                .completedAt(p.getCompletedAt())
                                .build();
        }

        private VideoCourseProgressResponse toCourseProgressResponse(VideoCourseProgress p) {
                return VideoCourseProgressResponse.builder()
                                .id(p.getId())
                                .studySetId(p.getStudySetId())
                                .status(p.getStatus())
                                .completedSteps(p.getCompletedSteps())
                                .totalSteps(p.getTotalSteps())
                                .progressPercentage(p.getProgressPercentage())
                                .currentStepId(p.getCurrentStepId())
                                .firstStartedAt(p.getFirstStartedAt())
                                .completedAt(p.getCompletedAt())
                                .build();
        }
}
