package com.lms.learningpath.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.learningpath.dto.request.CreateUnlockRuleRequest;
import com.lms.learningpath.dto.request.UpdateUnlockRuleRequest;
import com.lms.learningpath.dto.response.UnlockRuleResponse;
import com.lms.learningpath.entity.StudySetUnlockRule;
import com.lms.learningpath.mapper.UnlockRuleMapper;
import com.lms.learningpath.repository.StudySetUnlockRuleRepository;
import com.lms.learningpath.service.IUnlockRuleManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing StudySet unlock rules (CRUD operations).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnlockRuleManagementServiceImpl implements IUnlockRuleManagementService {

    private final StudySetUnlockRuleRepository unlockRuleRepository;

    @Override
    @Transactional
    public UnlockRuleResponse createUnlockRule(CreateUnlockRuleRequest request) {
        log.info("Creating unlock rule for studySetId: {}", request.getStudySetId());

        // Convert request to entity
        StudySetUnlockRule entity = UnlockRuleMapper.toEntity(request);

        // Save to database
        StudySetUnlockRule saved = unlockRuleRepository.save(entity);

        log.info("Created unlock rule with ID: {}", saved.getId());
        return UnlockRuleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UnlockRuleResponse updateUnlockRule(String id, UpdateUnlockRuleRequest request) {
        log.info("Updating unlock rule: {}", id);

        // Find existing rule
        StudySetUnlockRule entity = unlockRuleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Unlock rule not found with ID: " + id));

        // Update entity
        UnlockRuleMapper.updateEntity(entity, request);

        // Save changes
        StudySetUnlockRule updated = unlockRuleRepository.save(entity);

        log.info("Updated unlock rule: {}", id);
        return UnlockRuleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUnlockRule(String id) {
        log.info("Deleting unlock rule: {}", id);

        // Find existing rule
        StudySetUnlockRule entity = unlockRuleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Unlock rule not found with ID: " + id));

        // Soft delete - set isActive to false
        entity.setIsActive(false);
        unlockRuleRepository.save(entity);

        log.info("Deleted (soft) unlock rule: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UnlockRuleResponse getUnlockRuleById(String id) {
        log.debug("Getting unlock rule by ID: {}", id);

        StudySetUnlockRule entity = unlockRuleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.E227, "Unlock rule not found with ID: " + id));

        return UnlockRuleMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnlockRuleResponse> getUnlockRulesByStudySet(String studySetId) {
        log.debug("Getting unlock rules for studySetId: {}", studySetId);

        List<StudySetUnlockRule> rules = unlockRuleRepository.findByStudySetId(studySetId);

        return rules.stream()
                .map(UnlockRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnlockRuleResponse> getUnlockRulesRequiringStudySet(String studySetId) {
        log.debug("Getting unlock rules that require studySetId: {}", studySetId);

        List<StudySetUnlockRule> rules = unlockRuleRepository.findByRequiredStudySetId(studySetId);

        return rules.stream()
                .map(UnlockRuleMapper::toResponse)
                .collect(Collectors.toList());
    }
}
