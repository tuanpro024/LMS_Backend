package com.lms.videocourse.service;

import com.lms.videocourse.dto.request.CompleteWatchRequest;
import com.lms.videocourse.dto.request.UpdateWatchProgressRequest;
import com.lms.videocourse.dto.response.VideoCourseProgressResponse;
import com.lms.videocourse.dto.response.VideoStepProgressResponse;
import com.lms.videocourse.dto.response.VideoWatchProgressResponse;

import java.util.List;

public interface IVideoProgressService {

    /** Start watching a video module — creates/returns progress with IN_PROGRESS */
    VideoWatchProgressResponse startWatching(String userId, String moduleId);

    /**
     * Update watch progress. Auto-completes module (and triggers rollup) when
     * watchPercent >= 80.
     */
    VideoWatchProgressResponse updateWatchProgress(String userId, String moduleId, UpdateWatchProgressRequest request);

    /** Force-complete a video module regardless of watch percentage */
    VideoWatchProgressResponse completeWatch(String userId, String moduleId, CompleteWatchRequest request);

    /** Get watch progress for a single module */
    VideoWatchProgressResponse getWatchProgress(String userId, String moduleId);

    /** Get step progress (aggregated from all modules in the step) */
    VideoStepProgressResponse getStepProgress(String userId, String stepId);

    /** Get course progress (aggregated from all steps in the course) */
    VideoCourseProgressResponse getCourseProgress(String userId, String courseId);

    /** Get all course progress for a user in a study set */
    List<VideoCourseProgressResponse> getAllCourseProgress(String userId, String studySetId);
}
