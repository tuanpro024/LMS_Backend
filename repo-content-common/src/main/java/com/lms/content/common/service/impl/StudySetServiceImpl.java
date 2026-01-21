package com.lms.content.common.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.content.common.dto.request.CreateStudySetRequest;
import com.lms.content.common.dto.request.UpdateStudySetRequest;
import com.lms.content.common.dto.response.StudySetResponse;
import com.lms.content.common.entity.StudySet;
import com.lms.content.common.mapper.StudySetMapper;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.StudySetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudySetServiceImpl implements StudySetService {

    private final StudySetRepository studySetRepository;
    private final StudySetMapper studySetMapper;

    @Override
    public StudySetResponse createStudySet(CreateStudySetRequest request, String userId) {
        log.info("Creating study set for user: {}", userId);

        // Hook: validate
        validateCreateStudySet(request, userId);

        StudySet studySet = studySetMapper.toEntity(request);
        studySet.setUserId(userId);

        // Hook: before save
        beforeSaveStudySet(studySet, request);

        StudySet saved = studySetRepository.save(studySet);

        // Hook: after save
        afterSaveStudySet(saved);

        return studySetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudySetResponse getStudySetById(String id) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));
        return studySetMapper.toResponse(studySet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByUserId(String userId) {
        List<StudySet> studySets = studySetRepository.findByUserId(userId);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByFolderId(String folderId) {
        List<StudySet> studySets = studySetRepository.findByFolderId(folderId);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> searchStudySets(String keyword) {
        List<StudySet> studySets = studySetRepository.searchByKeyword(keyword);
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getAllStudySets() {
        List<StudySet> studySets = studySetRepository.findAll();
        return studySetMapper.toResponseList(studySets);
    }

    @Override
    public StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to modify this study set");
        }

        // Hook: validate update
        validateUpdateStudySet(studySet, request, userId);

        if (request.getTitle() != null) {
            studySet.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            studySet.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            studySet.setPrivate(request.getIsPrivate());
        }

        // Hook: before update
        beforeUpdateStudySet(studySet, request);

        StudySet updated = studySetRepository.save(studySet);

        // Hook: after update
        afterUpdateStudySet(updated);

        return studySetMapper.toResponse(updated);
    }

    @Override
    public void deleteStudySet(String id, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "StudySet not found"));

        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this study set");
        }

        // Hook: before delete
        beforeDeleteStudySet(studySet, userId);

        studySetRepository.delete(studySet);

        // Hook: after delete
        afterDeleteStudySet(id, userId);
    }

    // ========== EXTENSION HOOKS ==========

    protected void validateCreateStudySet(CreateStudySetRequest request, String userId) {
    }

    protected void beforeSaveStudySet(StudySet studySet, CreateStudySetRequest request) {
    }

    protected void afterSaveStudySet(StudySet saved) {
    }

    protected void validateUpdateStudySet(StudySet entity, UpdateStudySetRequest request, String userId) {
    }

    protected void beforeUpdateStudySet(StudySet entity, UpdateStudySetRequest request) {
    }

    protected void afterUpdateStudySet(StudySet updated) {
    }

    protected void beforeDeleteStudySet(StudySet entity, String userId) {
    }

    protected void afterDeleteStudySet(String studySetId, String userId) {
    }
}
