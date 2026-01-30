package com.lms.learningpath.service.impl;

import com.lms.learningpath.client.FlashcardServiceClient;
import com.lms.learningpath.client.KanjiOriginServiceClient;
import com.lms.learningpath.client.WritingServiceClient;
import com.lms.learningpath.dto.external.StudySetDto;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.mapper.StudySetMapper;
import com.lms.learningpath.service.IModuleIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of service for integrating with external learning modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleIntegrationServiceImpl implements IModuleIntegrationService {

    private final FlashcardServiceClient flashcardClient;
    private final KanjiOriginServiceClient kanjiClient;
    private final WritingServiceClient writingClient;

    @Override
    public Optional<StudySetDto> getStudySet(ModuleType moduleType, String studySetId) {
        try {
            log.info("Getting StudySet {} from module type {}", studySetId, moduleType);

            return switch (moduleType) {
                case FLASHCARD -> Optional.ofNullable(flashcardClient.getStudySetById(studySetId))
                        .map(response -> response.data())
                        .map(StudySetMapper::toDto);
                case KANJI -> Optional.ofNullable(kanjiClient.getStudySetById(studySetId))
                        .map(response -> response.data())
                        .map(StudySetMapper::toDto);
                case WRITING -> Optional.ofNullable(writingClient.getStudySetById(studySetId))
                        .map(response -> response.data())
                        .map(StudySetMapper::toDto);
                default -> {
                    log.warn("Module type {} not yet supported for Feign client integration", moduleType);
                    yield Optional.empty();
                }
            };
        } catch (Exception e) {
            log.error("Failed to get StudySet from {}: {}", moduleType, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<StudySetDto> getAllStudySets(ModuleType moduleType) {
        try {
            log.info("Getting all StudySets from module type {}", moduleType);

            return switch (moduleType) {
                case FLASHCARD -> Optional.ofNullable(flashcardClient.getAllStudySets())
                        .map(response -> response.data())
                        .map(list -> list.stream()
                                .map(StudySetMapper::toDto)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                case KANJI -> Optional.ofNullable(kanjiClient.getAllStudySets())
                        .map(response -> response.data())
                        .map(list -> list.stream()
                                .map(StudySetMapper::toDto)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                case WRITING -> Optional.ofNullable(writingClient.getAllStudySets())
                        .map(response -> response.data())
                        .map(list -> list.stream()
                                .map(StudySetMapper::toDto)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                default -> {
                    log.warn("Module type {} not yet supported for Feign client integration", moduleType);
                    yield List.of();
                }
            };
        } catch (Exception e) {
            log.error("Failed to get StudySets from {}: {}", moduleType, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<StudySetDto> searchStudySets(ModuleType moduleType, String query) {
        try {
            log.info("Searching StudySets in {} with query: {}", moduleType, query);

            return switch (moduleType) {
                case FLASHCARD -> Optional.ofNullable(flashcardClient.searchStudySets(query))
                        .map(response -> response.data())
                        .map(list -> list.stream()
                                .map(StudySetMapper::toDto)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                case KANJI -> Optional.ofNullable(kanjiClient.searchStudySets(query))
                        .map(response -> response.data())
                        .map(list -> list.stream()
                                .map(StudySetMapper::toDto)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                case WRITING -> Optional.ofNullable(writingClient.searchStudySets(query))
                        .map(response -> response.data())
                        .map(list -> list.stream()
                                .map(StudySetMapper::toDto)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                default -> {
                    log.warn("Module type {} not yet supported for Feign client integration", moduleType);
                    yield List.of();
                }
            };
        } catch (Exception e) {
            log.error("Failed to search StudySets in {}: {}", moduleType, e.getMessage());
            return List.of();
        }
    }

    @Override
    public int getContentSetItemCount(ModuleType moduleType, String contentSetId) {
        if (contentSetId == null) {
            return 0;
        }
        return getStudySet(moduleType, contentSetId)
                .map(dto -> dto.getItemCount() != null ? dto.getItemCount().intValue() : 0)
                .orElse(0);
    }
}
