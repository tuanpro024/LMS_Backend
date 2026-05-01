package com.lms.pronunciation.service.impl;

import com.lms.content.common.entity.StudySet;
import com.lms.content.common.mapper.StudySetMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.impl.StudySetServiceImpl;
import com.lms.pronunciation.repository.PronunciationItemRepository;
import com.lms.pronunciation.repository.PronunciationItemStudySetProgressRepository;
import com.lms.pronunciation.repository.UserPronunciationItemProgressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pronunciation-specific override của StudySetServiceImpl.
 * <p>
 * Mục đích chính: Xóa các bản ghi pronunciation items và progress thuộc về {@code StudySet}
 * trước khi xóa {@code StudySet} để tránh vi phạm FK constraint.
 * <p>
 * Thứ tự xóa (đảm bảo không vi phạm FK):
 * <ol>
 *   <li>{@code user_pronunciation_item_progress} — FK pronunciation_item_id → pronunciation_items.id</li>
 *   <li>{@code pronunciation_item_study_set_progress}</li>
 *   <li>{@code pronunciation_items}               — FK study_set_id → study_sets.id</li>
 * </ol>
 */
@Service
@Primary
@Slf4j
@Transactional
public class PronunciationStudySetServiceImpl extends StudySetServiceImpl {

    private final PronunciationItemRepository pronunciationItemRepository;
    private final UserPronunciationItemProgressRepository userPronunciationItemProgressRepository;
    private final PronunciationItemStudySetProgressRepository pronunciationItemStudySetProgressRepository;

    public PronunciationStudySetServiceImpl(
            StudySetRepository studySetRepository,
            FolderRepository folderRepository,
            PackageRepository packageRepository,
            StudySetMapper studySetMapper,
            ApplicationEventPublisher eventPublisher,
            PronunciationItemRepository pronunciationItemRepository,
            UserPronunciationItemProgressRepository userPronunciationItemProgressRepository,
            PronunciationItemStudySetProgressRepository pronunciationItemStudySetProgressRepository) {
        super(studySetRepository, folderRepository, packageRepository, studySetMapper, eventPublisher);
        this.pronunciationItemRepository = pronunciationItemRepository;
        this.userPronunciationItemProgressRepository = userPronunciationItemProgressRepository;
        this.pronunciationItemStudySetProgressRepository = pronunciationItemStudySetProgressRepository;
    }

    @Override
    protected void beforeDeleteStudySet(StudySet entity, String userId) {
        String studySetId = entity.getId();
        log.info("[PronunciationStudySetServiceImpl] Deleting pronunciation data for studySetId={}", studySetId);

        // 1. Xóa user progress per-item (FK pronunciation_item_id → pronunciation_items)
        userPronunciationItemProgressRepository.deleteByPronunciationItemStudySetId(studySetId);

        // 2. Xóa study set level progress
        pronunciationItemStudySetProgressRepository.deleteByStudySetId(studySetId);

        // 3. Xóa pronunciation items (FK study_set_id → study_sets.id)
        pronunciationItemRepository.deleteByStudySetId(studySetId);

        log.info("[PronunciationStudySetServiceImpl] Deleted pronunciation data for studySetId={}", studySetId);
    }
}
