package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiLessonProgress;
import com.lms.kanjiorigin.repository.KanjiLessonProgressRepository;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.service.KanjiLessonProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiLessonProgressServiceImpl implements KanjiLessonProgressService {

    private final KanjiLessonProgressRepository progressRepository;
    private final KanjiLessonRepository kanjiLessonRepository;

    @Override
    public void markLessonAsLearned(String lessonId, String userId) {
        log.info("Marking lesson {} as learned for user {}", lessonId, userId);

        KanjiLesson lesson = kanjiLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + lessonId));

        KanjiLessonProgress progress = progressRepository.findByUserIdAndKanjiLessonId(userId, lessonId)
                .orElse(KanjiLessonProgress.builder()
                        .userId(userId)
                        .kanjiLesson(lesson)
                        .isLearned(false)
                        .build());

        progress.setLearned(true);
        progressRepository.save(progress);
    }

    @Override
    public void unmarkLessonAsLearned(String lessonId, String userId) {
        log.info("Unmarking lesson {} as learned for user {}", lessonId, userId);
        
        progressRepository.findByUserIdAndKanjiLessonId(userId, lessonId)
                .ifPresent(progress -> {
                    progress.setLearned(false);
                    progressRepository.save(progress);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getLearnedLessonIds(String userId) {
        return progressRepository.findByUserId(userId).stream()
                .filter(KanjiLessonProgress::isLearned)
                .map(p -> p.getKanjiLesson().getId())
                .collect(Collectors.toList());
    }
}
