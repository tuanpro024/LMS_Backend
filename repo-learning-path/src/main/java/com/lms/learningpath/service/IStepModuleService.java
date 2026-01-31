package com.lms.learningpath.service;

import com.lms.learningpath.dto.request.AddModuleToStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.response.StepModuleResponse;

import java.util.List;

/**
 * Service for managing practice modules within Steps.
 */
public interface IStepModuleService {

    /**
     * Add a module to a step
     */
    StepModuleResponse addModuleToStep(AddModuleToStepRequest request, String userId);

    /**
     * Get module by ID
     */
    StepModuleResponse getModuleById(String id);

    /**
     * Get all modules for a step
     */
    List<StepModuleResponse> getModulesByStepId(String stepId);

    /**
     * Remove a module from a step
     */
    void removeModuleFromStep(String moduleId, String userId);

    /**
     * Reorder modules within a step
     */
    void reorderModules(String stepId, ReorderItemsRequest request);
}
