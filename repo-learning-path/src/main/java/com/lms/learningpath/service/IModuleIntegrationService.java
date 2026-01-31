package com.lms.learningpath.service;

import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.entity.enums.ModuleType;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for integrating with external learning modules.
 */
public interface IModuleIntegrationService {

    /**
     * Get StudySet from external module.
     *
     * @param moduleType the module type
     * @param studySetId the StudySet ID
     * @return optional StudySet DTO
     */
    Optional<StudySetDto> getStudySet(ModuleType moduleType, String studySetId);

    /**
     * Get all StudySets from a module type.
     *
     * @param moduleType the module type
     * @return list of StudySet DTOs
     */
    List<StudySetDto> getAllStudySets(ModuleType moduleType);

    /**
     * Search StudySets in a module.
     *
     * @param moduleType the module type
     * @param query      search query
     * @return list of StudySet DTOs
     */
    List<StudySetDto> searchStudySets(ModuleType moduleType, String query);

    /**
     * Get item count from content set.
     *
     * @param moduleType   the module type
     * @param contentSetId the content set ID
     * @return item count
     */
    int getContentSetItemCount(ModuleType moduleType, String contentSetId);
}
