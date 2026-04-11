package com.lms.learningpath.service.impl;

import com.lms.content.common.entity.StudySet;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.learningpath.exception.ResourceAlreadyExistsException;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.dto.request.CreateLearningPathRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.request.UpdateLearningPathRequest;
import com.lms.learningpath.dto.response.LearningPathResponse;
import com.lms.learningpath.entity.LearningPath;
import com.lms.learningpath.mapper.LearningPathMapper;
import com.lms.learningpath.repository.LearningPathProgressRepository;
import com.lms.learningpath.repository.LearningPathRepository;
import com.lms.learningpath.repository.StepRepository;
import com.lms.learningpath.service.ILearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathServiceImpl implements ILearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathProgressRepository learningPathProgressRepository;
    private final StepRepository stepRepository;
    private final StudySetRepository studySetRepository;
    private final LearningPathMapper learningPathMapper;

    @Override
    @Transactional
    public LearningPathResponse createLearningPath(CreateLearningPathRequest request, String userId) {
        log.info("Creating learning path: {} for study set: {}", request.getTitle(), request.getStudySetId());

        StudySet studySet = studySetRepository.findById(request.getStudySetId())
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study set not found with id: " + request.getStudySetId()));

        LearningPath learningPath = learningPathMapper.toEntity(request, userId);
        learningPath.setStudySet(studySet);
        learningPath = learningPathRepository.save(learningPath);

        log.info("Successfully created learning path: {}", learningPath.getId());
        return learningPathMapper.toResponse(learningPath);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponse getLearningPathById(String id) {
        LearningPath learningPath = findLearningPathById(id);
        LearningPathResponse response = learningPathMapper.toResponse(learningPath);

        // Add step count
        long totalSteps = stepRepository.countByLearningPathIdAndIsActiveTrue(id);
        response.setTotalSteps((int) totalSteps);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponse getLearningPathWithProgress(String id, String userId) {
        LearningPath learningPath = findLearningPathById(id);
        LearningPathResponse response = learningPathMapper.toResponse(learningPath);

        // Add step count
        long totalSteps = stepRepository.countByLearningPathIdAndIsActiveTrue(id);
        response.setTotalSteps((int) totalSteps);

        // Add user progress
        learningPathProgressRepository.findByUserIdAndLearningPathId(userId, id)
                .ifPresent(progress -> {
                    response.setCompletedSteps(progress.getCompletedSteps());
                    response.setProgressPercentage(progress.getProgressPercentage());
                });

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponse> getLearningPathsByStudySetId(String studySetId) {
        List<LearningPath> learningPaths = learningPathRepository
                .findByStudySetIdAndIsActiveTrueOrderByCreatedAtAsc(studySetId);

        return learningPaths.stream()
                .map(lp -> {
                    LearningPathResponse response = learningPathMapper.toResponse(lp);
                    long totalSteps = stepRepository.countByLearningPathIdAndIsActiveTrue(lp.getId());
                    response.setTotalSteps((int) totalSteps);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponse> getLearningPathsByStudySetIdWithProgress(String studySetId, String userId) {
        List<LearningPath> learningPaths = learningPathRepository
                .findByStudySetIdAndIsActiveTrueOrderByCreatedAtAsc(studySetId);

        return learningPaths.stream()
                .map(lp -> {
                    LearningPathResponse response = learningPathMapper.toResponse(lp);
                    long totalSteps = stepRepository.countByLearningPathIdAndIsActiveTrue(lp.getId());
                    response.setTotalSteps((int) totalSteps);

                    // Add user progress
                    learningPathProgressRepository.findByUserIdAndLearningPathId(userId, lp.getId())
                            .ifPresent(progress -> {
                                response.setCompletedSteps(progress.getCompletedSteps());
                                response.setProgressPercentage(progress.getProgressPercentage());
                            });

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LearningPathResponse updateLearningPath(String id, UpdateLearningPathRequest request, String userId) {
        log.info("Updating learning path: {}", id);

        LearningPath learningPath = findLearningPathById(id);
        learningPathMapper.updateEntity(learningPath, request);
        learningPath = learningPathRepository.save(learningPath);

        log.info("Successfully updated learning path: {}", id);
        return learningPathMapper.toResponse(learningPath);
    }

    @Override
    @Transactional
    public void deleteLearningPath(String id, String userId) {
        log.info("Deleting learning path: {}", id);

        LearningPath learningPath = findLearningPathById(id);
        learningPath.setIsActive(false);
        learningPathRepository.save(learningPath);

        log.info("Successfully soft-deleted learning path: {}", id);
    }

    private LearningPath findLearningPathById(String id) {
        return learningPathRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + id));
    }
}
