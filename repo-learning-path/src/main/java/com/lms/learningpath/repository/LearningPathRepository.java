package com.lms.learningpath.repository;

import com.lms.learningpath.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, String> {

    List<LearningPath> findByStudySet_IdOrderByCreatedAtAsc(String studySetId);

    Optional<LearningPath> findByIdAndIsActiveTrue(String id);

    List<LearningPath> findByStudySet_IdAndIsActiveTrueOrderByCreatedAtAsc(String studySetId);

    List<LearningPath> findByCreatedBy(String userId);

    boolean existsByIdAndIsActiveTrue(String id);

    long countByStudySet_Id(String studySetId);
}
