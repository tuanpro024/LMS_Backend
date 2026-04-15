package com.lms.learningpath.repository;

import com.lms.learningpath.entity.LearningPathProgress;
import com.lms.learningpath.entity.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathProgressRepository extends JpaRepository<LearningPathProgress, String> {

    Optional<LearningPathProgress> findByUserIdAndLearningPathId(String userId, String learningPathId);

    List<LearningPathProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<LearningPathProgress> findByUserIdAndStatus(String userId, ProgressStatus status);

    boolean existsByUserIdAndLearningPathId(String userId, String learningPathId);

    void deleteByLearningPathId(String learningPathId);
}
