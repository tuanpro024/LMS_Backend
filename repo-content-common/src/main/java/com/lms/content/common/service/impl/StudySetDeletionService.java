package com.lms.content.common.service.impl;

import com.lms.content.common.service.StudySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudySetDeletionService {

    private final StudySetService studySetService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteStudySetInNewTransaction(String studySetId, String userId) {
        studySetService.deleteStudySet(studySetId, userId);
    }
}
