package com.lms.pronunciation.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.delegate.api.StudySetApiDelegate;
import com.lms.pronunciation.dto.response.PronunciationItemProgressResponse;
import com.lms.pronunciation.dto.response.PronunciationStudySetProgressResponse;
import com.lms.pronunciation.entity.PronunciationItem;
import com.lms.pronunciation.entity.PronunciationItemStudySetProgress;
import com.lms.pronunciation.entity.UserPronunciationItemProgress;
import com.lms.pronunciation.entity.enums.ProgressStatus;
import com.lms.pronunciation.entity.enums.PronunciationLearningStatus;
import com.lms.pronunciation.repository.PronunciationItemRepository;
import com.lms.pronunciation.repository.PronunciationItemStudySetProgressRepository;
import com.lms.pronunciation.repository.UserPronunciationItemProgressRepository;
import com.lms.pronunciation.service.PronunciationProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PronunciationProgressServiceImpl implements PronunciationProgressService {

    private final UserPronunciationItemProgressRepository userItemProgressRepo;
    private final PronunciationItemStudySetProgressRepository studySetProgressRepo;
    private final PronunciationItemRepository itemRepo;
    private final StudySetApiDelegate studySetApiDelegate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PronunciationItemProgressResponse markItemListened(String userId, String itemId) {
        PronunciationItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Pronunciation item not found"));

        if (item.isDeleted()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot mark deleted item as listened");
        }

        if (item.getStudySet() != null) {
            studySetApiDelegate.assertStudySetLearningAllowed(item.getStudySet().getId());
        }

        UserPronunciationItemProgress progress = userItemProgressRepo.findByUserIdAndPronunciationItemId(userId, itemId)
                .orElse(UserPronunciationItemProgress.builder()
                        .userId(userId)
                        .pronunciationItem(item)
                        .status(PronunciationLearningStatus.NOT_LEARNED)
                        .listenCount(0)
                        .build());

        Instant now = Instant.now();
        if (progress.getFirstListenedAt() == null) {
            progress.setFirstListenedAt(now);
        }
        progress.setLastListenedAt(now);
        progress.setListenCount(progress.getListenCount() + 1);
        progress.setStatus(PronunciationLearningStatus.LEARNED);

        userItemProgressRepo.save(progress);

        // Update Study Set Progress
        if (item.getStudySet() != null) {
            updateStudySetProgress(userId, item.getStudySet().getId());
        }

        return PronunciationItemProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .pronunciationItemId(item.getId())
                .status(progress.getStatus())
                .firstListenedAt(progress.getFirstListenedAt())
                .lastListenedAt(progress.getLastListenedAt())
                .listenCount(progress.getListenCount())
                .build();
    }

    @Override
    public PronunciationStudySetProgressResponse getStudySetProgress(String userId, String studySetId) {
        // Only recalculate/write when the study set is still learnable.
        if (studySetApiDelegate.isStudySetLearningAllowed(studySetId)) {
            updateStudySetProgress(userId, studySetId);
        }

        PronunciationItemStudySetProgress progress = studySetProgressRepo.findByUserIdAndStudySetId(userId, studySetId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST,
                        "Progress not found for study set: " + studySetId));

        return PronunciationStudySetProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .studySetId(progress.getStudySetId())
                .status(progress.getStatus())
                .learnedItems(progress.getLearnedItems())
                .totalItems(progress.getTotalItems())
                .progressPercentage(progress.getProgressPercentage())
                .firstStartedAt(progress.getFirstStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }

    @Override
    public java.util.List<PronunciationItemProgressResponse> getItemProgressByStudySet(String userId,
            String studySetId) {
        return userItemProgressRepo.findByUserIdAndStudySetId(userId, studySetId).stream()
                .map(p -> PronunciationItemProgressResponse.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .pronunciationItemId(p.getPronunciationItem().getId())
                        .status(p.getStatus())
                        .firstListenedAt(p.getFirstListenedAt())
                        .lastListenedAt(p.getLastListenedAt())
                        .listenCount(p.getListenCount())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void updateStudySetProgress(String userId, String studySetId) {
        PronunciationItemStudySetProgress progress = studySetProgressRepo.findByUserIdAndStudySetId(userId, studySetId)
                .orElse(PronunciationItemStudySetProgress.builder()
                        .userId(userId)
                        .studySetId(studySetId)
                        .learnedItems(0)
                        .totalItems(0)
                        .status(ProgressStatus.NOT_STARTED)
                        .build());

        Integer previousLearnedItems = progress.getLearnedItems();
        Integer previousTotalItems = progress.getTotalItems();
        Double previousProgressPercentage = progress.getProgressPercentage();
        ProgressStatus previousStatus = progress.getStatus();
        Instant previousCompletedAt = progress.getCompletedAt();

        long totalItems = itemRepo.countByStudySetIdAndDeletedFalse(studySetId);
        long learnedItems = userItemProgressRepo.countByUserIdAndStudySetIdAndStatus(userId, studySetId,
                PronunciationLearningStatus.LEARNED);

        progress.setTotalItems((int) totalItems);
        progress.setLearnedItems((int) learnedItems);
        progress.updateProgressPercent();

        Instant now = Instant.now();
        if (learnedItems > 0 && progress.getFirstStartedAt() == null) {
            progress.setFirstStartedAt(now);
        }

        if (totalItems > 0 && learnedItems >= totalItems) {
            if (progress.getStatus() != ProgressStatus.COMPLETED) {
                progress.setStatus(ProgressStatus.COMPLETED);
                progress.setCompletedAt(now);
            }
        } else if (learnedItems > 0) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
            progress.setCompletedAt(null);
        } else {
            progress.setStatus(ProgressStatus.NOT_STARTED);
            progress.setCompletedAt(null);
        }

        studySetProgressRepo.save(progress);

        boolean progressChanged = !Objects.equals(previousLearnedItems, progress.getLearnedItems())
                || !Objects.equals(previousTotalItems, progress.getTotalItems())
                || !Objects.equals(previousProgressPercentage, progress.getProgressPercentage())
                || previousStatus != progress.getStatus()
                || !Objects.equals(previousCompletedAt, progress.getCompletedAt());

        if (!progressChanged) {
            log.debug(
                    "Skip publishing pronunciation progress event because state is unchanged for userId: {}, studySetId: {}",
                    userId, studySetId);
            return;
        }

        com.lms.pronunciation.event.PronunciationStudySetProgressUpdatedEvent event = com.lms.pronunciation.event.PronunciationStudySetProgressUpdatedEvent
                .builder()
                .userId(userId)
                .studySetId(studySetId)
                .learnedItems((int) learnedItems)
                .totalItems((int) totalItems)
                .progressPercentage(progress.getProgressPercentage())
                .completed(progress.getStatus() == ProgressStatus.COMPLETED)
                .completedAt(progress.getCompletedAt())
                .occurredAt(Instant.now())
                .build();

        eventPublisher.publishEvent(event);
    }
}
