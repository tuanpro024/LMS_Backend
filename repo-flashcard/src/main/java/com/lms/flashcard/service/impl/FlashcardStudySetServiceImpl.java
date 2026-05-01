package com.lms.flashcard.service.impl;

import com.lms.content.common.entity.StudySet;
import com.lms.content.common.mapper.StudySetMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.impl.StudySetServiceImpl;
import com.lms.flashcard.repository.CardRepository;
import com.lms.flashcard.repository.FlashcardStudySetProgressRepository;
import com.lms.flashcard.repository.UserCardProgressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Flashcard-specific override của StudySetServiceImpl.
 * <p>
 * Mục đích chính: Xóa các bản ghi {@code Card}, {@code UserCardProgress}
 * và {@code FlashcardStudySetProgress} thuộc về {@code StudySet}
 * trước khi xóa {@code StudySet} để tránh vi phạm FK constraint.
 */
@Service
@Primary
@Slf4j
@Transactional
public class FlashcardStudySetServiceImpl extends StudySetServiceImpl {

    private final CardRepository cardRepository;
    private final UserCardProgressRepository userCardProgressRepository;
    private final FlashcardStudySetProgressRepository flashcardStudySetProgressRepository;

    public FlashcardStudySetServiceImpl(
            StudySetRepository studySetRepository,
            FolderRepository folderRepository,
            PackageRepository packageRepository,
            StudySetMapper studySetMapper,
            ApplicationEventPublisher eventPublisher,
            CardRepository cardRepository,
            UserCardProgressRepository userCardProgressRepository,
            FlashcardStudySetProgressRepository flashcardStudySetProgressRepository) {
        super(studySetRepository, folderRepository, packageRepository, studySetMapper, eventPublisher);
        this.cardRepository = cardRepository;
        this.userCardProgressRepository = userCardProgressRepository;
        this.flashcardStudySetProgressRepository = flashcardStudySetProgressRepository;
    }

    @Override
    protected void beforeDeleteStudySet(StudySet entity, String userId) {
        String studySetId = entity.getId();
        log.info("[FlashcardStudySetServiceImpl] Deleting flashcard data for studySetId={}", studySetId);

        // 1. Xóa user progress records (FK → cards)
        userCardProgressRepository.deleteByCardStudySetId(studySetId);

        // 2. Xóa study set progress records
        flashcardStudySetProgressRepository.deleteByStudySetId(studySetId);

        // 3. Xóa tất cả cards (FK → study_sets)
        cardRepository.deleteByStudySetId(studySetId);

        log.info("[FlashcardStudySetServiceImpl] Deleted flashcard data for studySetId={}", studySetId);
    }
}
