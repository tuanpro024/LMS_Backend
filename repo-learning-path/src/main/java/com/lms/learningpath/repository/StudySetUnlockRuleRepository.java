package com.lms.learningpath.repository;

import com.lms.learningpath.entity.StudySetUnlockRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudySetUnlockRuleRepository extends JpaRepository<StudySetUnlockRule, String> {

    Optional<StudySetUnlockRule> findByStudySetIdAndIsActive(String studySetId, Boolean isActive);

    List<StudySetUnlockRule> findByStudySetId(String studySetId);

    List<StudySetUnlockRule> findByRequiredStudySetId(String requiredStudySetId);

    void deleteByStudySetId(String studySetId);
}
