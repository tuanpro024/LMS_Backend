package com.lms.learningpath.service;

import com.lms.learningpath.dto.response.AvailableModuleResponse;

import java.util.List;

/**
 * Service for fetching available study sets from other microservices
 * Used by Admin/Teacher when selecting modules to add to steps.
 */
public interface IAvailableModuleService {

    /**
     * Get all available study sets from all repos
     */
    List<AvailableModuleResponse> getAllAvailableModules(String query);

    /**
     * Get available flashcard study sets
     */
    List<AvailableModuleResponse> getAvailableFlashcardSets(String query);

    /**
     * Get available writing study sets
     */
    List<AvailableModuleResponse> getAvailableWritingSets(String query);

    /**
     * Get available kanji-origin study sets
     */
    List<AvailableModuleResponse> getAvailableKanjiSets(String query);

    /**
     * Get available quiz study sets
     */
    List<AvailableModuleResponse> getAvailableQuizSets(String query);
}
