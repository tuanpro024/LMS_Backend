package com.lms.videocourse.service.impl;

import com.lms.common.dto.ApiResponse;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.videocourse.client.FlashcardClient;
import com.lms.videocourse.client.KanjiOriginClient;
import com.lms.videocourse.client.ListeningPracticeClient;
import com.lms.videocourse.client.PronunciationClient;
import com.lms.videocourse.client.QuizClient;
import com.lms.videocourse.client.WritingClient;
import com.lms.videocourse.dto.response.AvailableModuleResponse;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.service.IAvailableModuleService;
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

    private final FlashcardClient flashcardClient;
    private final WritingClient writingClient;
    private final KanjiOriginClient kanjiClient;
    private final QuizClient quizClient;
    private final ListeningPracticeClient listeningClient;
    private final PronunciationClient pronunciationClient;

    @Override
    public List<AvailableModuleResponse> getAllAvailableModules(String query) {
        log.info("Fetching all available modules with query: {}", query);
        List<AvailableModuleResponse> all = new ArrayList<>();
        all.addAll(getAvailableFlashcardSets(query));
        all.addAll(getAvailableWritingSets(query));
        all.addAll(getAvailableKanjiSets(query));
        all.addAll(getAvailableQuizSets(query));
        all.addAll(getAvailableListeningSets(query));
        all.addAll(getAvailablePronunciationSets(query));
        log.info("Found {} available modules across all repos", all.size());
        return all;
    }

    @Override
    public List<AvailableModuleResponse> getAvailableFlashcardSets(String query) {
        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? flashcardClient.searchStudySets(query)
                    : flashcardClient.getAllStudySets();
            return toResponseList(response.data(), ModuleType.FLASHCARD, "repo-flashcard");
        } catch (Exception e) {
            log.error("Error fetching flashcard sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailableWritingSets(String query) {
        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? writingClient.searchStudySets(query)
                    : writingClient.getAllStudySets();
            return toResponseList(response.data(), ModuleType.WRITING, "repo-writing");
        } catch (Exception e) {
            log.error("Error fetching writing sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailableKanjiSets(String query) {
        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? kanjiClient.searchStudySets(query)
                    : kanjiClient.getAllStudySets();
            return toResponseList(response.data(), ModuleType.KANJI_ORIGIN, "repo-kanji-origin");
        } catch (Exception e) {
            log.error("Error fetching kanji sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailableQuizSets(String query) {
        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? quizClient.searchStudySets(query)
                    : quizClient.getAllStudySets();
            return toResponseList(response.data(), ModuleType.QUIZ, "repo-quiz");
        } catch (Exception e) {
            log.error("Error fetching quiz sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailableListeningSets(String query) {
        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? listeningClient.searchStudySets(query)
                    : listeningClient.getAllStudySets();
            return toResponseList(response.data(), ModuleType.LISTENING, "repo-listening-practice");
        } catch (Exception e) {
            log.error("Error fetching listening practice sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AvailableModuleResponse> getAvailablePronunciationSets(String query) {
        try {
            ApiResponse<List<StudySetResponse>> response = (query != null && !query.isBlank())
                    ? pronunciationClient.searchStudySets(query)
                    : pronunciationClient.getAllStudySets();
            return toResponseList(response.data(), ModuleType.PRONUNCIATION, "repo-pronunciation");
        } catch (Exception e) {
            log.error("Error fetching pronunciation sets: {}", e.getMessage());
            return new ArrayList<>();
        }
    }


    private List<AvailableModuleResponse> toResponseList(
            List<StudySetResponse> studySets, ModuleType type, String repoName) {
        if (studySets == null)
            return new ArrayList<>();
        return studySets.stream()
                .map(s -> AvailableModuleResponse.builder()
                        .id(s.getId())
                        .title(s.getTitle())
                        .description(s.getDescription())
                        .thumbnail(s.getThumbnail())
                        .moduleType(type)
                        .repoName(repoName)
                        .itemCount((long) s.getTotalItems())
                        .isPrivate(s.isPrivate())
                        .userId(s.getUserId())
                        .build())
                .collect(Collectors.toList());
    }
}
