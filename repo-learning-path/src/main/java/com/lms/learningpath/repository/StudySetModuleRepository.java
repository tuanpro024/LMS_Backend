package com.lms.learningpath.repository;

import com.lms.learningpath.entity.StudySetModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudySetModuleRepository extends JpaRepository<StudySetModule, String> {

    List<StudySetModule> findByStudySetIdAndIsActiveOrderByModuleOrder(String studySetId, Boolean isActive);

    List<StudySetModule> findByStudySetIdOrderByModuleOrder(String studySetId);

    long countByStudySetIdAndIsActive(String studySetId, Boolean isActive);

    long countByStudySetIdAndIsRequiredAndIsActive(String studySetId, Boolean isRequired, Boolean isActive);

    List<StudySetModule> findByStudySetIdAndModuleType(String studySetId,
            com.lms.learningpath.entity.enums.ModuleType moduleType);

    void deleteByStudySetId(String studySetId);
}
