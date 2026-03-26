package com.lms.videocourse.service.impl;

import com.lms.content.common.entity.TypeName;
import com.lms.videocourse.client.FlashcardClient;
import com.lms.videocourse.client.KanjiOriginClient;
import com.lms.videocourse.client.ListeningPracticeClient;
import com.lms.videocourse.client.PronunciationClient;
import com.lms.videocourse.client.QuizClient;
import com.lms.videocourse.client.WritingClient;
import com.lms.videocourse.dto.response.AvailableModuleResponse;
import com.lms.videocourse.entity.enums.ModuleType;
import com.lms.videocourse.service.IAvailableModuleService;
import com.lms.videocourse.service.helper.VideoCourseStudySetFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    private final VideoCourseStudySetFetcher studySetFetcher;

    @Override
    public List<AvailableModuleResponse> getAllAvailableModules(String query) {
        log.info("Fetching all available modules (VIDEO_COURSE only) with query: {}", query);
        List<AvailableModuleResponse> all = new ArrayList<>();
        all.addAll(getAvailableFlashcardSets(query));
        all.addAll(getAvailableWritingSets(query));
        all.addAll(getAvailableKanjiSets(query));
        all.addAll(getAvailableQuizSets(query));
        all.addAll(getAvailableListeningSets(query));
        all.addAll(getAvailablePronunciationSets(query));
        log.info("Found {} available modules across all repos (VIDEO_COURSE filtered)", all.size());
        return all;
    }

    @Override
    public List<AvailableModuleResponse> getAvailableFlashcardSets(String query) {
        return studySetFetcher.fetch(
                () -> flashcardClient.getPackagesByType(TypeName.VIDEO_COURSE),
                flashcardClient::getStudySetsByFolderId,
                ModuleType.FLASHCARD, "repo-flashcard", query);
    }

    @Override
    public List<AvailableModuleResponse> getAvailableWritingSets(String query) {
        return studySetFetcher.fetch(
                () -> writingClient.getPackagesByType(TypeName.VIDEO_COURSE),
                writingClient::getStudySetsByFolderId,
                ModuleType.WRITING, "repo-writing", query);
    }

    @Override
    public List<AvailableModuleResponse> getAvailableKanjiSets(String query) {
        return studySetFetcher.fetch(
                () -> kanjiClient.getPackagesByType(TypeName.VIDEO_COURSE),
                kanjiClient::getStudySetsByFolderId,
                ModuleType.KANJI_ORIGIN, "repo-kanji-origin", query);
    }

    @Override
    public List<AvailableModuleResponse> getAvailableQuizSets(String query) {
        return studySetFetcher.fetch(
                () -> quizClient.getPackagesByType(TypeName.VIDEO_COURSE),
                quizClient::getStudySetsByFolderId,
                ModuleType.QUIZ, "repo-quiz", query);
    }

    @Override
    public List<AvailableModuleResponse> getAvailableListeningSets(String query) {
        return studySetFetcher.fetch(
                () -> listeningClient.getPackagesByType(TypeName.VIDEO_COURSE),
                listeningClient::getStudySetsByFolderId,
                ModuleType.LISTENING, "repo-listening-practice", query);
    }

    @Override
    public List<AvailableModuleResponse> getAvailablePronunciationSets(String query) {
        return studySetFetcher.fetch(
                () -> pronunciationClient.getPackagesByType(TypeName.VIDEO_COURSE),
                pronunciationClient::getStudySetsByFolderId,
                ModuleType.PRONUNCIATION, "repo-pronunciation", query);
    }
}
