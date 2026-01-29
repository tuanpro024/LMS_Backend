package com.lms.learningpath.repository;

import com.lms.learningpath.entity.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningModuleRepository extends JpaRepository<LearningModule, String> {

    List<LearningModule> findByStudySetIdOrderByOrderIndexAsc(String studySetId);

    List<LearningModule> findByStudySetIdAndIsRequiredTrue(String studySetId);

    @Query("SELECT COUNT(m) FROM LearningModule m WHERE m.studySetId = :studySetId AND m.isRequired = true")
    long countRequiredByStudySetId(@Param("studySetId") String studySetId);

    @Query("SELECT COUNT(m) FROM LearningModule m WHERE m.studySetId = :studySetId")
    long countByStudySetId(@Param("studySetId") String studySetId);
}