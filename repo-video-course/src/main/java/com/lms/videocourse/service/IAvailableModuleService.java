package com.lms.videocourse.service;

import com.lms.videocourse.dto.response.AvailableModuleResponse;

import java.util.List;

/**
 * Service for fetching available study sets from other microservices.
 * Used by Admin/Teacher when selecting modules to associate with video steps.
 */
public interface IAvailableModuleService {

    /** Get all available study sets from all repos */
    List<AvailableModuleResponse> getAllAvailableModules(String query);

    /** Get available flashcard study sets */
    List<AvailableModuleResponse> getAvailableFlashcardSets(String query);

    /** Get available writing study sets */
    List<AvailableModuleResponse> getAvailableWritingSets(String query);

    /** Get available kanji study sets */
    List<AvailableModuleResponse> getAvailableKanjiSets(String query);

    /** Get available quiz study sets */
    List<AvailableModuleResponse> getAvailableQuizSets(String query);

    /** Get available listening practice study sets */
    List<AvailableModuleResponse> getAvailableListeningSets(String query);

    /** Get available pronunciation study sets */
    List<AvailableModuleResponse> getAvailablePronunciationSets(String query);

    /** Get available videos from repo-multimedia */
    List<AvailableModuleResponse> getAvailableVideoSets(String query);

}
