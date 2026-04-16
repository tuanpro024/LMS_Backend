package com.lms.kanjiorigin.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.kanjiorigin.dto.request.UpdateKanjiStatusRequest;
import com.lms.kanjiorigin.dto.response.KanjiStatusResponse;
import com.lms.kanjiorigin.dto.response.KanjiStudySetProgressResponse;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import com.lms.kanjiorigin.entity.KanjiStudySetProgress;
import com.lms.kanjiorigin.entity.UserKanjiProgress;
import com.lms.kanjiorigin.entity.enums.KanjiStatus;
import com.lms.kanjiorigin.entity.enums.StudySetProgressStatus;
import com.lms.kanjiorigin.event.KanjiStudySetProgressUpdatedEvent;
import com.lms.kanjiorigin.repository.KanjiOriginRepository;
import com.lms.kanjiorigin.repository.KanjiStudySetProgressRepository;
import com.lms.kanjiorigin.repository.UserKanjiProgressRepository;
import com.lms.kanjiorigin.service.KanjiProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KanjiProgressServiceImpl implements KanjiProgressService {

    private final UserKanjiProgressRepository userKanjiProgressRepository;
    private final KanjiStudySetProgressRepository kanjiStudySetProgressRepository;
    private final KanjiOriginRepository kanjiOriginRepository;
    private final StudySetRepository studySetRepository;
    private final StudySetApiDelegate studySetApiDelegate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public KanjiStatusResponse updateKanjiStatus(String userId, String kanjiId, UpdateKanjiStatusRequest request) {
        KanjiOrigin origin = kanjiOriginRepository.findById(kanjiId)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "KanjiOrigin not found with id: " + kanjiId));

        if (origin.getStudySet() != null) {
            studySetApiDelegate.assertStudySetLearningAllowed(origin.getStudySet().getId());
        }

        UserKanjiProgress progress = userKanjiProgressRepository.findByUserIdAndKanjiOriginId(userId, kanjiId)
                .orElseGet(() -> UserKanjiProgress.builder()
                        .userId(userId)
                        .kanjiOrigin(origin)
                        .status(KanjiStatus.NOT_LEARNED)
                        .reviewCount(0)
                        .build());

        progress.setStatus(request.getStatus());
        progress.setLastReviewedAt(Instant.now());
        if (request.getStatus() == KanjiStatus.LEARNED) {
            progress.setReviewCount(progress.getReviewCount() + 1);
        }

        UserKanjiProgress saved = userKanjiProgressRepository.save(progress);
        recalculateStudySetProgress(userId, origin.getStudySet().getId(), true);

        return toKanjiStatusResponse(origin, saved);
    }

    @Override
    public KanjiStudySetProgressResponse getStudySetProgress(String userId, String studySetId) {
        validateStudySetExists(studySetId);
        KanjiStudySetProgress progress = recalculateStudySetProgress(userId, studySetId, false);
        return toStudySetProgressResponse(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanjiStatusResponse> getStudySetKanjiStatuses(String userId, String studySetId) {
        validateStudySetExists(studySetId);

        List<KanjiOrigin> origins = kanjiOriginRepository.findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(studySetId);
        Map<String, UserKanjiProgress> progressByKanjiId = userKanjiProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .stream()
                .collect(Collectors.toMap(p -> p.getKanjiOrigin().getId(), Function.identity(), (a, b) -> a));

        return origins.stream()
                .map(origin -> toKanjiStatusResponse(origin, progressByKanjiId.get(origin.getId())))
                .toList();
    }

    private void validateStudySetExists(String studySetId) {
        if (!studySetRepository.existsById(studySetId)) {
            throw new ApiException(ErrorCode.E227, "StudySet not found with id: " + studySetId);
        }
    }

    private KanjiStudySetProgress recalculateStudySetProgress(String userId, String studySetId, boolean publishEvent) {
        long totalKanjis = kanjiOriginRepository.countByStudySetIdAndDeletedFalse(studySetId);
        long learnedKanjis = userKanjiProgressRepository.countByUserIdAndStudySetIdAndStatus(
                userId,
                studySetId,
                KanjiStatus.LEARNED);

        StudySetProgressStatus status;
        if (totalKanjis == 0) {
            status = StudySetProgressStatus.COMPLETED;
        } else if (learnedKanjis == 0) {
            status = StudySetProgressStatus.NOT_STARTED;
        } else if (learnedKanjis >= totalKanjis) {
            status = StudySetProgressStatus.COMPLETED;
        } else {
            status = StudySetProgressStatus.IN_PROGRESS;
        }

        KanjiStudySetProgress progress = kanjiStudySetProgressRepository
                .findByUserIdAndStudySetId(userId, studySetId)
                .orElseGet(() -> KanjiStudySetProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .status(StudySetProgressStatus.NOT_STARTED)
                        .learnedLessons(0)
                        .totalLessons((int) totalKanjis)
                        .progressPercentage(0.0)
                        .build());

        progress.setTotalLessons((int) totalKanjis);
        progress.setLearnedLessons((int) learnedKanjis);
        progress.setStatus(status);
        progress.setProgressPercentage(totalKanjis > 0 ? (double) learnedKanjis / totalKanjis : 1.0);

        Instant now = Instant.now();
        if (progress.getFirstStartedAt() == null && learnedKanjis > 0) {
            progress.setFirstStartedAt(now);
        }

        if (status == StudySetProgressStatus.COMPLETED) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(now);
            }
        } else {
            progress.setCompletedAt(null);
        }

        KanjiStudySetProgress saved = kanjiStudySetProgressRepository.save(progress);

        if (publishEvent) {
            eventPublisher.publishEvent(KanjiStudySetProgressUpdatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userId(userId)
                    .studySetId(studySetId)
                    .learnedLessons(saved.getLearnedLessons())
                    .totalLessons(saved.getTotalLessons())
                    .progressPercentage(saved.getProgressPercentage())
                    .completed(saved.getStatus() == StudySetProgressStatus.COMPLETED)
                    .occurredAt(now)
                    .build());
        }

        return saved;
    }

    private KanjiStatusResponse toKanjiStatusResponse(KanjiOrigin origin, UserKanjiProgress progress) {
        return KanjiStatusResponse.builder()
                .kanjiId(origin.getId())
                .studySetId(origin.getStudySet().getId())
                .contentIndex(origin.getContentIndex())
                .term(origin.getTerm())
                .pinyin(origin.getPinyin())
                .meaning(origin.getMeaning())
                .status(progress != null ? progress.getStatus() : KanjiStatus.NOT_LEARNED)
                .reviewCount(progress != null ? progress.getReviewCount() : 0)
                .lastReviewedAt(progress != null ? progress.getLastReviewedAt() : null)
                .build();
    }

    private KanjiStudySetProgressResponse toStudySetProgressResponse(KanjiStudySetProgress progress) {
        return KanjiStudySetProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .studySetId(progress.getStudySetId())
                .status(progress.getStatus())
                .learnedLessons(progress.getLearnedLessons())
                .totalLessons(progress.getTotalLessons())
                .progressPercentage(progress.getProgressPercentage())
                .completed(progress.getStatus() == StudySetProgressStatus.COMPLETED)
                .firstStartedAt(progress.getFirstStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}