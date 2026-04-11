package com.lms.learningpath.repository;

import com.lms.learningpath.entity.StepModule;
import com.lms.learningpath.entity.enums.ModuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepModuleRepository extends JpaRepository<StepModule, String> {

    List<StepModule> findByStepIdOrderByModuleOrderAsc(String stepId);

    Optional<StepModule> findByIdAndIsActiveTrue(String id);

    List<StepModule> findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(String stepId);

    boolean existsByStepIdAndModuleOrder(String stepId, Integer moduleOrder);

    boolean existsByStepIdAndModuleOrderAndIsActiveTrue(String stepId, Integer moduleOrder);

    boolean existsByStepIdAndContentSetIdAndIsActiveTrue(String stepId, String contentSetId);

    long countByStepId(String stepId);

    long countByStepIdAndIsActiveTrue(String stepId);

    long countByStepIdAndIsRequiredTrue(String stepId);

    List<StepModule> findByModuleTypeAndContentSetIdAndIsActiveTrue(ModuleType moduleType, String contentSetId);

    List<StepModule> findByModuleTypeInAndContentSetIdAndIsActiveTrue(List<ModuleType> moduleTypes, String contentSetId);
}
