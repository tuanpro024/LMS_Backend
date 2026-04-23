package com.lms.writing.service.impl;


import com.lms.content.common.entity.StudySet;
import com.lms.content.common.mapper.StudySetMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.impl.StudySetServiceImpl;
import com.lms.writing.repository.WordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writing-specific override của StudySetServiceImpl.
 * <p>
 * Mục đích chính: Xóa các bản ghi {@code Word} thuộc về {@code StudySet}
 * trước khi xóa {@code StudySet} để tránh lỗi vi phạm ràng buộc khóa ngoại (foreign key constraint).
 */
@Service
@Primary
@Slf4j
@Transactional
public class WritingStudySetServiceImpl extends StudySetServiceImpl {

    private final WordRepository wordRepository;

    public WritingStudySetServiceImpl(
            StudySetRepository studySetRepository,
            FolderRepository folderRepository,
            PackageRepository packageRepository,
            StudySetMapper studySetMapper,
            ApplicationEventPublisher eventPublisher,
            WordRepository wordRepository) {
        super(studySetRepository, folderRepository, packageRepository, studySetMapper, eventPublisher);
        this.wordRepository = wordRepository;
    }

    @Override
    protected void beforeDeleteStudySet(StudySet entity, String userId) {
        String studySetId = entity.getId();
        log.info("[WritingStudySetServiceImpl] Deleting words associated with studySetId={}", studySetId);
        
        // Custom query to delete by studySetId. 
        wordRepository.deleteByStudySetId(studySetId);
        log.info("[WritingStudySetServiceImpl] Deleted words for studySetId={}", studySetId);
    }
}
