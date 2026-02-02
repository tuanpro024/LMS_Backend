package com.lms.learningpath.repository;

import com.lms.learningpath.entity.StepUnlockRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StepUnlockRuleRepository extends JpaRepository<StepUnlockRule, String> {

    Optional<StepUnlockRule> findByStepId(String stepId);

    Optional<StepUnlockRule> findByStepIdAndIsActiveTrue(String stepId);

    boolean existsByStepId(String stepId);
}
