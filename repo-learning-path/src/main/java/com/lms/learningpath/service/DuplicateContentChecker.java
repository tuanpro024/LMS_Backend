package com.lms.learningpath.service;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.learningpath.client.*;
import com.lms.learningpath.entity.enums.ModuleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Service to check for duplicate content across external repositories.
 * Implements search-before-create strategy to avoid creating duplicate content.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DuplicateContentChecker {

    private final FlashcardClient flashcardClient;
    private final WritingClient writingClient;
    private final KanjiOriginClient kanjiClient;
    private final QuizClient quizClient;
    private final PronunciationClient pronunciationClient;
    // Note: PronunciationServiceClient can be added when needed

    /**
     * Find existing StudySet in external repository by title (exact
     * case-insensitive match).
     * 
     * @param title      StudySet title to search for
     * @param moduleType Type of module (determines which repo to search)
     * @return Optional containing contentSetId if found
     */
    public Optional<String> findExistingContentSet(String title, ModuleType moduleType) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        try {
            switch (moduleType) {
                case FLASHCARD:
                    return searchByTitle(flashcardClient.searchStudySets(title), title);

                case WRITING:
                    return searchByTitle(writingClient.searchStudySets(title), title);

                case KANJI:
                    return searchByTitle(kanjiClient.searchStudySets(title), title);

                case QUIZ:
                    return searchByTitle(quizClient.searchStudySets(title), title);

                case PRONUNCIATION:
                    return searchByTitle(pronunciationClient.searchStudySets(title), title);

                // Add other types as needed
                default:
                    log.debug("Duplicate check not implemented for module type: {}", moduleType);
                    return Optional.empty();
            }
        } catch (Exception e) {
            log.warn("Error checking duplicate for '{}' ({}): {}", title, moduleType, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Search for exact title match in response list.
     */
    private Optional<String> searchByTitle(ApiResponse<List<StudySetResponse>> response, String title) {
        if (response == null || response.data() == null) {
            return Optional.empty();
        }

        return response.data().stream()
                .filter(s -> s.getTitle() != null && s.getTitle().equalsIgnoreCase(title.trim()))
                .map(StudySetResponse::getId)
                .findFirst();
    }
}
