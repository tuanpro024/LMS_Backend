package com.lms.learningpath.repository;

import com.lms.learningpath.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, String> {

    List<LearningPath> findByStudySetIdOrderByDisplayOrderAsc(String studySetId);

    Optional<LearningPath> findByIdAndIsActiveTrue(String id);

    List<LearningPath> findByStudySetIdAndIsActiveTrueOrderByDisplayOrderAsc(String studySetId);

    boolean existsByStudySetIdAndDisplayOrder(String studySetId, Integer displayOrder);

    List<LearningPath> findByCreatedBy(String userId);

    boolean existsByIdAndIsActiveTrue(String id);

    long countByStudySetId(String studySetId);
}
