package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.CreateStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.request.UpdateStepRequest;
import com.lms.learningpath.dto.response.StepResponse;

import java.util.List;

/**
 * Service for managing Steps within Learning Paths.
 */
public interface IStepService {

    /**
     * Create a new step (also creates unlock rule for sequential unlocking)
     */
    StepResponse createStep(CreateStepRequest request, String userId);

    /**
     * Get step by ID
     */
    StepResponse getStepById(String id);

    /**
     * Get step with user progress and unlock status
     */
    StepResponse getStepWithProgress(String id, String userId);

    /**
     * Get all steps for a learning path
     */
    List<StepResponse> getStepsByLearningPathId(String learningPathId);

    /**
     * Get all steps with user progress and unlock status
     */
    List<StepResponse> getStepsByLearningPathIdWithProgress(String learningPathId, String userId);

    /**
     * Update a step
     */
    StepResponse updateStep(String id, UpdateStepRequest request, String userId);

    /**
     * Delete a step
     */
    void deleteStep(String id, String userId);

    /**
     * Reorder steps within a learning path
     */
    void reorderSteps(String learningPathId, ReorderItemsRequest request);
}
