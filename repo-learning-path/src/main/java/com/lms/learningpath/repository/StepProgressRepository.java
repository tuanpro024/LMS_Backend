package com.lms.learningpath.repository;

import com.lms.learningpath.entity.StepProgress;
import com.lms.learningpath.entity.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepProgressRepository extends JpaRepository<StepProgress, String> {

    Optional<StepProgress> findByUserIdAndStepId(String userId, String stepId);

    List<StepProgress> findByUserIdAndStepIdIn(String userId, List<String> stepIds);

    List<StepProgress> findByUserIdAndLearningPathId(String userId, String learningPathId);

    List<StepProgress> findByUserIdAndStatus(String userId, ProgressStatus status);

    boolean existsByUserIdAndStepId(String userId, String stepId);

    long countByUserIdAndLearningPathIdAndStatus(String userId, String learningPathId, ProgressStatus status);
}
