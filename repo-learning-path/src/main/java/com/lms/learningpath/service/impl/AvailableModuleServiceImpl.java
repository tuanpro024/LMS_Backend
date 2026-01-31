package com.lms.learningpath.service.impl;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.learningpath.client.FlashcardServiceClient;
import com.lms.learningpath.client.KanjiOriginServiceClient;
import com.lms.learningpath.client.WritingServiceClient;
import com.lms.learningpath.dto.response.AvailableModuleResponse;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.service.IAvailableModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableModuleServiceImpl implements IAvailableModuleService {

    private final FlashcardServiceClient flashcardServiceClient;
    private final WritingServiceClient writingServiceClient;
    private final KanjiOriginServiceClient kanjiOriginServiceClient;

    @Override
    public List<AvailableModuleResponse> getAllAvailableModules(String query) {
        log.info("Fetching all available modules with query: {}", query);

        List<AvailableModuleResponse> allModules = new ArrayList<>();

        // Fetch from all repos
        allModules.addAll(getAvailableFlashcardSets(query));
        allModules.addAll(getAvailableWritingSets(query));
        allModules.addAll(getAvailableKanjiSets(query));

        log.info("Found {} available modules across all repos", allModules.size());
        return allModules;
    }

    @Override
    public List<AvailableModuleResponse> getAvailableFlashcardSets(String query) {
        log.info("Fetching available flashcard sets");

        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? flashcardServiceClient.searchStudySets(query)
                    : flashcardServiceClient.getAllStudySets();

            List<StudySetResponse> studySets = response.data();
            if (studySets == null) {
                return new ArrayList<>();
            }

            return studySets.stream()
                    .map(dto -> toAvailableModuleResponse(dto, ModuleType.FLASHCARD, "repo-flashcard"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching flashcard sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailableWritingSets(String query) {
        log.info("Fetching available writing sets");

        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? writingServiceClient.searchStudySets(query)
                    : writingServiceClient.getAllStudySets();

            List<StudySetResponse> studySets = response.data();
            if (studySets == null) {
                return new ArrayList<>();
            }

            return studySets.stream()
                    .map(dto -> toAvailableModuleResponse(dto, ModuleType.WRITING, "repo-writing"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching writing sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailableKanjiSets(String query) {
        log.info("Fetching available kanji sets");

        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? kanjiOriginServiceClient.searchStudySets(query)
                    : kanjiOriginServiceClient.getAllStudySets();

            List<StudySetResponse> studySets = response.data();
            if (studySets == null) {
                return new ArrayList<>();
            }

            return studySets.stream()
                    .map(dto -> toAvailableModuleResponse(dto, ModuleType.KANJI, "repo-kanji-origin"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching kanji sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private AvailableModuleResponse toAvailableModuleResponse(
            StudySetResponse dto, ModuleType moduleType, String repoName) {
        return AvailableModuleResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .moduleType(moduleType)
                .repoName(repoName)
                .folderId(null) // Not available in StudySetResponse
                .folderName(null) // Not available in StudySetResponse
                .itemCount((long) dto.getTotalItems()) // Convert int to Long
                .isPrivate(dto.isPrivate())
                .userId(dto.getUserId())
                .build();
    }
}
