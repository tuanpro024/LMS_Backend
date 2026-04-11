package com.lms.learningpath.service.impl;

import com.lms.learningpath.client.FlashcardClient;
import com.lms.learningpath.client.KanjiOriginClient;
import com.lms.learningpath.client.PronunciationClient;
import com.lms.learningpath.client.QuizClient;
import com.lms.learningpath.client.WritingClient;
import com.lms.learningpath.exception.ResourceAlreadyExistsException;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.dto.request.AddModuleToStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.response.StepModuleResponse;
import com.lms.learningpath.entity.Step;
import com.lms.learningpath.entity.StepModule;
import com.lms.learningpath.entity.enums.ModuleType;
import com.lms.learningpath.mapper.StepModuleMapper;
import com.lms.learningpath.repository.StepModuleRepository;
import com.lms.learningpath.repository.StepRepository;
import com.lms.learningpath.service.IStepModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepModuleServiceImpl implements IStepModuleService {

    private final StepModuleRepository stepModuleRepository;
    private final StepRepository stepRepository;
    private final StepModuleMapper stepModuleMapper;
    private final FlashcardClient flashcardClient;
    private final WritingClient writingClient;
    private final KanjiOriginClient kanjiOriginClient;
    private final QuizClient quizClient;
    private final PronunciationClient pronunciationClient;

    @Override
    @Transactional
    public StepModuleResponse addModuleToStep(AddModuleToStepRequest request, String userId) {
        log.info("Adding module to step: {}", request.getStepId());

        Step step = stepRepository.findByIdAndIsActiveTrue(request.getStepId())
            .orElseThrow(() -> new ResourceNotFoundException("Step not found with id: " + request.getStepId()));

        ModuleType normalizedModuleType = normalizeModuleType(request.getModuleType());
        request.setModuleType(normalizedModuleType);

        // Check if module order already exists
        if (stepModuleRepository.existsByStepIdAndModuleOrderAndIsActiveTrue(
                request.getStepId(), request.getModuleOrder())) {
            throw new ResourceAlreadyExistsException(
                    "Module with order " + request.getModuleOrder() + " already exists in this step");
        }

        if (stepModuleRepository.existsByStepIdAndContentSetIdAndIsActiveTrue(
            step.getId(), request.getContentSetId())) {
            throw new ResourceAlreadyExistsException(
                "Content set " + request.getContentSetId() + " already exists in this step");
        }

        validateContentSetMapping(normalizedModuleType, request.getContentSetId());

        StepModule stepModule = stepModuleMapper.toEntity(request);
        stepModule = stepModuleRepository.save(stepModule);

        log.info("Successfully added module: {} to step: {}", stepModule.getId(), request.getStepId());
        return stepModuleMapper.toResponse(stepModule);
    }

    @Override
    public StepModuleResponse getModuleById(String id) {
        StepModule stepModule = findStepModuleById(id);
        return stepModuleMapper.toResponse(stepModule);
    }

    @Override
    public List<StepModuleResponse> getModulesByStepId(String stepId) {
        List<StepModule> modules = stepModuleRepository
                .findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(stepId);

        return modules.stream()
                .map(stepModuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeModuleFromStep(String moduleId, String userId) {
        log.info("Removing module: {} from step", moduleId);

        StepModule stepModule = findStepModuleById(moduleId);
        stepModule.setIsActive(false);
        stepModuleRepository.save(stepModule);

        log.info("Successfully removed module: {}", moduleId);
    }

    @Override
    @Transactional
    public void reorderModules(String stepId, ReorderItemsRequest request) {
        log.info("Reordering modules for step: {}", stepId);

        if (!stepRepository.existsByIdAndIsActiveTrue(stepId)) {
            throw new ResourceNotFoundException("Step not found with id: " + stepId);
        }

        List<StepModule> activeModules = stepModuleRepository
                .findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(stepId);
        Map<String, StepModule> activeModuleById = activeModules.stream()
                .collect(Collectors.toMap(StepModule::getId, module -> module));

        Set<Integer> requestedOrders = new HashSet<>();
        Map<String, Integer> overrides = new HashMap<>();
        List<StepModule> modulesToUpdate = new java.util.ArrayList<>();

        for (ReorderItemsRequest.ReorderItem item : request.getItems()) {
            StepModule stepModule = activeModuleById.get(item.getId());
            if (stepModule == null) {
                throw new ResourceNotFoundException(
                        "Step module " + item.getId() + " does not belong to step: " + stepId);
            }

            if (!requestedOrders.add(item.getNewOrder())) {
                throw new ResourceAlreadyExistsException(
                        "Duplicate module order " + item.getNewOrder() + " in reorder request");
            }

            stepModule.setModuleOrder(item.getNewOrder());
            overrides.put(stepModule.getId(), item.getNewOrder());
            modulesToUpdate.add(stepModule);
        }

        Set<Integer> finalOrders = new HashSet<>();
        for (StepModule module : activeModules) {
            int finalOrder = overrides.getOrDefault(module.getId(), module.getModuleOrder());
            if (!finalOrders.add(finalOrder)) {
                throw new ResourceAlreadyExistsException(
                        "Module order conflict detected after reorder in step: " + stepId);
            }
        }

        if (!modulesToUpdate.isEmpty()) {
            stepModuleRepository.saveAll(modulesToUpdate);
        }

        log.info("Successfully reordered {} modules", request.getItems().size());
    }

    private StepModule findStepModuleById(String id) {
        return stepModuleRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Step module not found with id: " + id));
    }

    private ModuleType normalizeModuleType(ModuleType moduleType) {
        if (moduleType == null) {
            return null;
        }
        return moduleType == ModuleType.KANJI_ORIGIN ? ModuleType.KANJI : moduleType;
    }

    private void validateContentSetMapping(ModuleType moduleType, String contentSetId) {
        if (contentSetId == null || contentSetId.isBlank() || moduleType == null) {
            throw new ResourceNotFoundException("Invalid module mapping: moduleType/contentSetId is missing");
        }

        try {
            boolean exists = switch (moduleType) {
                case FLASHCARD -> flashcardClient.getStudySetById(contentSetId).data() != null;
                case WRITING -> writingClient.getStudySetById(contentSetId).data() != null;
                case KANJI, KANJI_ORIGIN -> kanjiOriginClient.getStudySetById(contentSetId).data() != null;
                case QUIZ -> quizClient.getStudySetById(contentSetId).data() != null;
                case PRONUNCIATION -> pronunciationClient.getStudySetById(contentSetId).data() != null;
                default -> true;
            };

            if (!exists) {
                throw new ResourceNotFoundException(
                        "Content set " + contentSetId + " not found for module type " + moduleType);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException(
                    "Content set " + contentSetId + " is invalid for module type " + moduleType);
        }
    }
}
