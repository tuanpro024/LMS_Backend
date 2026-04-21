package com.lms.onllearning.service.impl;

import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.onllearning.client.PracticeModuleWebClient;
import com.lms.onllearning.dto.response.AvailableScheduleModuleResponse;
import com.lms.onllearning.entity.enums.ScheduleModuleType;
import com.lms.onllearning.service.IAvailableScheduleModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lấy danh sách study set có sẵn từ các repo ôn luyện thông qua PracticeModuleWebClient.
 * Mỗi call thất bại sẽ bị bỏ qua (trả về []) để không chặn toàn bộ response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableScheduleModuleServiceImpl implements IAvailableScheduleModuleService {

    private final PracticeModuleWebClient webClient;

    @Value("${practice.service.flashcard-url:http://repo-flashcard}")
    private String flashcardUrl;

    @Value("${practice.service.writing-url:http://repo-writing}")
    private String writingUrl;

    @Value("${practice.service.kanji-url:http://repo-kanji-origin}")
    private String kanjiUrl;

    @Value("${practice.service.pronunciation-url:http://repo-pronunciation}")
    private String pronunciationUrl;

    @Value("${practice.service.quiz-url:http://repo-quiz}")
    private String quizUrl;

    @Value("${practice.service.listening-url:http://repo-listening-practice/api/listening-practice}")
    private String listeningUrl;

    @Override
    public List<AvailableScheduleModuleResponse> getAllAvailableModules(String query) {
        log.info("[AvailableScheduleModule] Fetching all modules, query={}", query);
        List<AvailableScheduleModuleResponse> all = new ArrayList<>();
        all.addAll(fetch(flashcardUrl,     ScheduleModuleType.FLASHCARD,     "repo-flashcard",     query));
        all.addAll(fetch(writingUrl,       ScheduleModuleType.WRITING,       "repo-writing",       query));
        all.addAll(fetch(kanjiUrl,         ScheduleModuleType.KANJI,         "repo-kanji-origin",  query));
        all.addAll(fetch(pronunciationUrl, ScheduleModuleType.PRONUNCIATION, "repo-pronunciation", query));
        all.addAll(fetch(quizUrl,          ScheduleModuleType.QUIZ,          "repo-quiz",          query));
        all.addAll(fetch(listeningUrl,     ScheduleModuleType.LISTENING,     "repo-listening-practice", query));
        log.info("[AvailableScheduleModule] Total {} modules found", all.size());
        return all;
    }

    @Override
    public List<AvailableScheduleModuleResponse> getAvailableModulesByType(ScheduleModuleType type, String query) {
        return switch (type) {
            case FLASHCARD     -> fetch(flashcardUrl,     type, "repo-flashcard",     query);
            case WRITING       -> fetch(writingUrl,       type, "repo-writing",       query);
            case KANJI         -> fetch(kanjiUrl,         type, "repo-kanji-origin",  query);
            case PRONUNCIATION -> fetch(pronunciationUrl, type, "repo-pronunciation", query);
            case QUIZ          -> fetch(quizUrl,          type, "repo-quiz",          query);
            case LISTENING     -> fetch(listeningUrl,     type, "repo-listening-practice", query);
        };
    }

    // -----------------------------------------------------------------------

    private List<AvailableScheduleModuleResponse> fetch(
            String baseUrl, ScheduleModuleType moduleType, String repoName, String query) {
        try {
            List<StudySetResponse> sets = webClient.getStudySets(baseUrl, query);
            return sets.stream()
                    .map(s -> toResponse(s, moduleType, repoName))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[AvailableScheduleModule] Failed to fetch from {}: {}", repoName, e.getMessage());
            return new ArrayList<>();
        }
    }

    private AvailableScheduleModuleResponse toResponse(
            StudySetResponse s, ScheduleModuleType moduleType, String repoName) {
        return AvailableScheduleModuleResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .thumbnail(s.getThumbnail())
                .moduleType(moduleType)
                .repoName(repoName)
                .itemCount((long) s.getTotalItems())
                .isPrivate(s.isPrivate())
                .userId(s.getUserId())
                .build();
    }
}
