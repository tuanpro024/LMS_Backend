package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.kanjiorigin.entity.KanjiLesson;
import com.lms.kanjiorigin.entity.KanjiLessonProgress;
import com.lms.kanjiorigin.entity.KanjiStudySetProgress;
import com.lms.kanjiorigin.entity.enums.StudySetProgressStatus;
import com.lms.kanjiorigin.event.KanjiStudySetProgressUpdatedEvent;
import com.lms.kanjiorigin.repository.KanjiLessonProgressRepository;
import com.lms.kanjiorigin.repository.KanjiLessonRepository;
import com.lms.kanjiorigin.repository.KanjiStudySetProgressRepository;
import com.lms.kanjiorigin.service.KanjiLessonProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiLessonProgressServiceImpl implements KanjiLessonProgressService {

    private final KanjiLessonProgressRepository progressRepository;
    private final KanjiLessonRepository kanjiLessonRepository;
    private final KanjiStudySetProgressRepository studySetProgressRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void markLessonAsLearned(String lessonId, String userId) {
        log.info("Marking lesson {} as learned for user {}", lessonId, userId);

        KanjiLesson lesson = kanjiLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + lessonId));
        String studySetId = lesson.getStudySet().getId();

        KanjiLessonProgress progress = progressRepository.findByUserIdAndKanjiLesson_Id(userId, lessonId)
                .orElseGet(() -> KanjiLessonProgress.builder()
                        .userId(userId)
                        .kanjiLesson(lesson)
                        .isLearned(false)
                        .build());

        progress.setLearned(true);
        progressRepository.save(progress);

        recalculateAndPublishStudySetProgress(userId, studySetId);
    }

    @Override
    public void unmarkLessonAsLearned(String lessonId, String userId) {
        log.info("Unmarking lesson {} as learned for user {}", lessonId, userId);

        KanjiLesson lesson = kanjiLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiLesson not found with id: " + lessonId));
        String studySetId = lesson.getStudySet().getId();

        progressRepository.findByUserIdAndKanjiLesson_Id(userId, lessonId)
                .ifPresent(progress -> {
                    progress.setLearned(false);
                    progressRepository.save(progress);
                });

        recalculateAndPublishStudySetProgress(userId, studySetId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getLearnedLessonIds(String userId) {
        return progressRepository.findByUserId(userId).stream()
                .filter(KanjiLessonProgress::isLearned)
                .map(p -> p.getKanjiLesson().getId())
                .collect(Collectors.toList());
    }

    private void recalculateAndPublishStudySetProgress(String userId, String studySetId) {
        Instant now = Instant.now();
        long totalLessons = kanjiLessonRepository.countByStudySetIdAndDeletedFalse(studySetId);
        long learnedLessons = progressRepository
                .countByUserIdAndKanjiLesson_StudySet_IdAndIsLearnedTrue(userId, studySetId);

        StudySetProgressStatus status;
        if (totalLessons == 0 || learnedLessons == 0) {
            status = StudySetProgressStatus.NOT_STARTED;
        } else if (learnedLessons >= totalLessons) {
            status = StudySetProgressStatus.COMPLETED;
        } else {
            status = StudySetProgressStatus.IN_PROGRESS;
        }

        double progressPercentage = totalLessons == 0 ? 0.0 : (learnedLessons * 100.0) / totalLessons;

        KanjiStudySetProgress studySetProgress = studySetProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElseGet(() -> KanjiStudySetProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .status(StudySetProgressStatus.NOT_STARTED)
                        .learnedLessons(0)
                        .totalLessons((int) totalLessons)
                        .progressPercentage(0.0)
                        .build());

        studySetProgress.setStatus(status);
        studySetProgress.setLearnedLessons((int) learnedLessons);
        studySetProgress.setTotalLessons((int) totalLessons);
        studySetProgress.setProgressPercentage(progressPercentage);

        if (studySetProgress.getFirstStartedAt() == null && learnedLessons > 0) {
            studySetProgress.setFirstStartedAt(now);
        }

        if (status == StudySetProgressStatus.COMPLETED) {
            if (studySetProgress.getCompletedAt() == null) {
                studySetProgress.setCompletedAt(now);
            }
        } else {
            studySetProgress.setCompletedAt(null);
        }

        studySetProgressRepository.save(studySetProgress);

        eventPublisher.publishEvent(KanjiStudySetProgressUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .studySetId(studySetId)
                .learnedLessons((int) learnedLessons)
                .totalLessons((int) totalLessons)
                .progressPercentage(progressPercentage)
                .completed(status == StudySetProgressStatus.COMPLETED)
                .occurredAt(now)
                .build());
    }
}
