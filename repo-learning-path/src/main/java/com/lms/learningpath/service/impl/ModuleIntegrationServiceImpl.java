package com.lms.learningpath.service.impl;

import com.lms.learningpath.client.FlashcardServiceClient;
import com.lms.learningpath.client.KanjiOriginServiceClient;
import com.lms.learningpath.client.WritingServiceClient;
import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.service.ModuleIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ModuleIntegrationService.
 * Routes requests to appropriate Feign clients based on module type.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleIntegrationServiceImpl implements ModuleIntegrationService {

    private final FlashcardServiceClient flashcardClient;
    private final KanjiOriginServiceClient kanjiOriginClient;
    private final WritingServiceClient writingClient;

    @Override
    public Optional<StudySetDto> getStudySet(ModuleType moduleType, String studySetId) {
        try {
            return switch (moduleType) {
                case FLASHCARD -> Optional.ofNullable(flashcardClient.getStudySetById(studySetId).getData());
                case KANJI_ORIGIN -> Optional.ofNullable(kanjiOriginClient.getStudySetById(studySetId).getData());
                case WRITING -> Optional.ofNullable(writingClient.getStudySetById(studySetId).getData());
                default -> {
                    log.warn("Unsupported module type: {}", moduleType);
                    yield Optional.empty();
                }
            };
        } catch (Exception e) {
            log.error("Failed to fetch StudySet {} from module {}: {}", studySetId, moduleType, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<StudySetDto> getAllStudySets(ModuleType moduleType) {
        try {
            return switch (moduleType) {
                case FLASHCARD -> flashcardClient.getAllStudySets().getData();
                case KANJI_ORIGIN -> kanjiOriginClient.getAllStudySets().getData();
                case WRITING -> writingClient.getAllStudySets().getData();
                default -> {
                    log.warn("Unsupported module type: {}", moduleType);
                    yield List.of();
                }
            };
        } catch (Exception e) {
            log.error("Failed to fetch all StudySets from module {}: {}", moduleType, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<StudySetDto> searchStudySets(ModuleType moduleType, String query) {
        try {
            return switch (moduleType) {
                case FLASHCARD -> flashcardClient.searchStudySets(query).getData();
                case KANJI_ORIGIN -> kanjiOriginClient.searchStudySets(query).getData();
                case WRITING -> writingClient.searchStudySets(query).getData();
                default -> {
                    log.warn("Unsupported module type: {}", moduleType);
                    yield List.of();
                }
            };
        } catch (Exception e) {
            log.error("Failed to search StudySets in module {}: {}", moduleType, e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean isStudySetAvailable(ModuleType moduleType, String studySetId) {
        return getStudySet(moduleType, studySetId).isPresent();
    }
}
