package com.lms.learningpath.service;

import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.entity.enums.ModuleType;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for integrating with external module services.
 * Fetches StudySet data from flashcard, kanji-origin, writing, etc.
 */
public interface ModuleIntegrationService {

    /**
     * Fetch StudySet from appropriate module service based on moduleType
     */
    Optional<StudySetDto> getStudySet(ModuleType moduleType, String studySetId);

    /**
     * Fetch all StudySets from a specific module
     */
    List<StudySetDto> getAllStudySets(ModuleType moduleType);

    /**
     * Search StudySets in a specific module
     */
    List<StudySetDto> searchStudySets(ModuleType moduleType, String query);

    /**
     * Check if a StudySet exists and is available
     */
    boolean isStudySetAvailable(ModuleType moduleType, String studySetId);
}
