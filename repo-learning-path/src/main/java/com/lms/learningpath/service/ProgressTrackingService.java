package com.lms.learningpath.service;

import com.lms.learningpath.dto.response.SetCompletionResult;
import com.lms.learningpath.dto.response.SetProgressResponse;

public interface ProgressTrackingService {

    /**
     * Lấy tiến độ học của user trên study set
     */
    SetProgressResponse getSetProgress(String userId, String studySetId);

    /**
     * Cập nhật tiến độ học study set sau khi hoàn thành module
     */
    SetProgressResponse updateSetProgress(String userId, String studySetId);

    /**
     * Đánh dấu study set hoàn thành
     */
    SetCompletionResult completeSet(String userId, String studySetId);
}