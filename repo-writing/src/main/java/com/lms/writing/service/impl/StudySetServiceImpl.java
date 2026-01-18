package com.lms.writing.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.writing.dto.request.CreateStudySetRequest;
import com.lms.writing.dto.request.CreateWordRequest;
import com.lms.writing.dto.request.UpdateStudySetRequest;
import com.lms.writing.dto.response.StudySetResponse;
import com.lms.writing.entity.StudySet;
import com.lms.writing.entity.Word;
import com.lms.writing.mapper.StudySetMapper;
import com.lms.writing.mapper.WordMapper;
import com.lms.writing.repository.StudySetRepository;
import com.lms.writing.service.StudySetService;
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
    private final WordMapper wordMapper;

    @Override
    public StudySetResponse createStudySet(CreateStudySetRequest request, String userId) {
        log.info("Creating study set for user: {}", userId);

        StudySet studySet = studySetMapper.toEntity(request);
        studySet.setUserId(userId);

        // Add words if provided
        if (request.getWords() != null && !request.getWords().isEmpty()) {
            for (CreateWordRequest wordRequest : request.getWords()) {
                Word word = wordMapper.toEntity(wordRequest);
                studySet.addWord(word);
            }
        }

        StudySet saved = studySetRepository.save(studySet);
        return studySetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudySetResponse getStudySetById(String id, String currentUserId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check access permission
        if (studySet.isPrivate() && (currentUserId == null || !studySet.getUserId().equals(currentUserId))) {
            throw new ApiException(ErrorCode.E240, "No permission to view this study set");
        }

        StudySetResponse response = studySetMapper.toResponse(studySet);
        // Manually map words
        if (studySet.getWords() != null) {
            response.setWords(wordMapper.toResponseList(studySet.getWords()));
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getAllPublicStudySets() {
        List<StudySet> studySets = studySetRepository.findByIsPrivateFalse();
        return studySets.stream()
                .map(studySet -> {
                    StudySetResponse response = studySetMapper.toResponse(studySet);
                    if (studySet.getWords() != null) {
                        response.setWords(wordMapper.toResponseList(studySet.getWords()));
                    }
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudySetResponse> getStudySetsByUserId(String userId, String currentUserId) {
        List<StudySet> studySets;

        if (currentUserId != null && userId.equals(currentUserId)) {
            // User can see all their own study sets
            studySets = studySetRepository.findByUserId(userId);
        } else {
            // Others can only see public study sets
            studySets = studySetRepository.findByUserId(userId).stream()
                    .filter(s -> !s.isPrivate())
                    .toList();
        }

        return studySets.stream()
                .map(studySet -> {
                    StudySetResponse response = studySetMapper.toResponse(studySet);
                    if (studySet.getWords() != null) {
                        response.setWords(wordMapper.toResponseList(studySet.getWords()));
                    }
                    return response;
                })
                .toList();
    }

    @Override
    public StudySetResponse updateStudySet(String id, UpdateStudySetRequest request, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check ownership
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to update this study set");
        }

        // Update using mapper
        studySetMapper.updateEntity(studySet, request);

        StudySet updated = studySetRepository.save(studySet);
        return studySetMapper.toResponse(updated);
    }

    @Override
    public void deleteStudySet(String id, String userId) {
        StudySet studySet = studySetRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Study set not found"));

        // Check ownership
        if (!studySet.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.E240, "No permission to delete this study set");
        }

        studySetRepository.delete(studySet);
    }
}
