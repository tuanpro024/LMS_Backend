package com.lms.learningpath.service.impl;

import com.lms.learningpath.exception.ResourceAlreadyExistsException;
import com.lms.learningpath.exception.ResourceNotFoundException;
import com.lms.learningpath.dto.request.AddModuleToStepRequest;
import com.lms.learningpath.dto.request.ReorderItemsRequest;
import com.lms.learningpath.dto.response.StepModuleResponse;
import com.lms.learningpath.entity.StepModule;
import com.lms.learningpath.mapper.StepModuleMapper;
import com.lms.learningpath.repository.StepModuleRepository;
import com.lms.learningpath.service.IStepModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepModuleServiceImpl implements IStepModuleService {

    private final StepModuleRepository stepModuleRepository;
    private final StepModuleMapper stepModuleMapper;

    @Override
    @Transactional
    public StepModuleResponse addModuleToStep(AddModuleToStepRequest request, String userId) {
        log.info("Adding module to step: {}", request.getStepId());

        // Check if module order already exists
        if (stepModuleRepository.existsByStepIdAndModuleOrder(
                request.getStepId(), request.getModuleOrder())) {
            throw new ResourceAlreadyExistsException(
                    "Module with order " + request.getModuleOrder() + " already exists in this step");
        }

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

        for (ReorderItemsRequest.ReorderItem item : request.getItems()) {
            StepModule stepModule = findStepModuleById(item.getId());
            stepModule.setModuleOrder(item.getNewOrder());
            stepModuleRepository.save(stepModule);
        }

        log.info("Successfully reordered {} modules", request.getItems().size());
    }

    private StepModule findStepModuleById(String id) {
        return stepModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Step module not found with id: " + id));
    }
}
