package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CreateLearningPathRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.request.UpdateLearningPathRequest;
import com.lms.learningpath.dto.response.LearningPathResponse;

import java.util.List;

/**
 * Service for managing Learning Paths within StudySets.
 */
public interface ILearningPathService {

    /**
     * Create a new learning path
     */
    LearningPathResponse createLearningPath(CreateLearningPathRequest request, String userId);

    /**
     * Get learning path by ID
     */
    LearningPathResponse getLearningPathById(String id);

    /**
     * Get learning path with user progress
     */
    LearningPathResponse getLearningPathWithProgress(String id, String userId);

    /**
     * Get all learning paths for a study set
     */
    List<LearningPathResponse> getLearningPathsByStudySetId(String studySetId);

    /**
     * Get all learning paths for a study set with user progress
     */
    List<LearningPathResponse> getLearningPathsByStudySetIdWithProgress(String studySetId, String userId);

    /**
     * Update a learning path
     */
    LearningPathResponse updateLearningPath(String id, UpdateLearningPathRequest request, String userId);

    /**
     * Delete a learning path
     */
    void deleteLearningPath(String id, String userId);

}
