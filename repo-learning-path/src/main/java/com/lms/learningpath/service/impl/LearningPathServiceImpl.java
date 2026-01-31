package com.lms.learningpath.service.impl;

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
    private final LearningPathMapper learningPathMapper;

    @Override
    @Transactional
    public LearningPathResponse createLearningPath(CreateLearningPathRequest request, String userId) {
        log.info("Creating learning path: {} for study set: {}", request.getTitle(), request.getStudySetId());

        // Check if display order already exists
        if (learningPathRepository.existsByStudySetIdAndDisplayOrder(
                request.getStudySetId(), request.getDisplayOrder())) {
            throw new ResourceAlreadyExistsException(
                    "Learning path with display order " + request.getDisplayOrder() + " already exists");
        }

        LearningPath learningPath = learningPathMapper.toEntity(request, userId);
        learningPath = learningPathRepository.save(learningPath);

        log.info("Successfully created learning path: {}", learningPath.getId());
        return learningPathMapper.toResponse(learningPath);
    }

    @Override
    public LearningPathResponse getLearningPathById(String id) {
        LearningPath learningPath = findLearningPathById(id);
        LearningPathResponse response = learningPathMapper.toResponse(learningPath);

        // Add step count
        long totalSteps = stepRepository.countByLearningPathId(id);
        response.setTotalSteps((int) totalSteps);

        return response;
    }

    @Override
    public LearningPathResponse getLearningPathWithProgress(String id, String userId) {
        LearningPath learningPath = findLearningPathById(id);
        LearningPathResponse response = learningPathMapper.toResponse(learningPath);

        // Add step count
        long totalSteps = stepRepository.countByLearningPathId(id);
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
    public List<LearningPathResponse> getLearningPathsByStudySetId(String studySetId) {
        List<LearningPath> learningPaths = learningPathRepository
                .findByStudySetIdAndIsActiveTrueOrderByDisplayOrderAsc(studySetId);

        return learningPaths.stream()
                .map(lp -> {
                    LearningPathResponse response = learningPathMapper.toResponse(lp);
                    long totalSteps = stepRepository.countByLearningPathId(lp.getId());
                    response.setTotalSteps((int) totalSteps);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningPathResponse> getLearningPathsByStudySetIdWithProgress(String studySetId, String userId) {
        List<LearningPath> learningPaths = learningPathRepository
                .findByStudySetIdAndIsActiveTrueOrderByDisplayOrderAsc(studySetId);

        return learningPaths.stream()
                .map(lp -> {
                    LearningPathResponse response = learningPathMapper.toResponse(lp);
                    long totalSteps = stepRepository.countByLearningPathId(lp.getId());
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

    @Override
    @Transactional
    public void reorderLearningPaths(String studySetId, ReorderItemsRequest request) {
        log.info("Reordering learning paths for study set: {}", studySetId);

        for (ReorderItemsRequest.ReorderItem item : request.getItems()) {
            LearningPath learningPath = findLearningPathById(item.getId());
            learningPath.setDisplayOrder(item.getNewOrder());
            learningPathRepository.save(learningPath);
        }

        log.info("Successfully reordered {} learning paths", request.getItems().size());
    }

    private LearningPath findLearningPathById(String id) {
        return learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + id));
    }
}
