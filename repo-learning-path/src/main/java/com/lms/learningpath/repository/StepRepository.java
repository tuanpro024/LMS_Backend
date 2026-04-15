package com.lms.learningpath.repository;

import com.lms.learningpath.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends JpaRepository<Step, String> {

    List<Step> findByLearningPathIdOrderByStepOrderAsc(String learningPathId);

    Optional<Step> findByIdAndIsActiveTrue(String id);

    boolean existsByIdAndIsActiveTrue(String id);

    List<Step> findByLearningPathIdAndIsActiveTrueOrderByStepOrderAsc(String learningPathId);

    boolean existsByLearningPathIdAndStepOrder(String learningPathId, Integer stepOrder);

    boolean existsByLearningPathIdAndStepOrderAndIsActiveTrue(String learningPathId, Integer stepOrder);

    Optional<Step> findByLearningPathIdAndStepOrder(String learningPathId, Integer stepOrder);

    Optional<Step> findByLearningPathIdAndStepOrderAndIsActiveTrue(String learningPathId, Integer stepOrder);

    long countByLearningPathId(String learningPathId);

    long countByLearningPathIdAndIsActiveTrue(String learningPathId);

    void deleteByLearningPathId(String learningPathId);
}
