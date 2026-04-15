package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusStepRepository extends JpaRepository<SyllabusStep, String> {
    List<SyllabusStep> findAllByDeletedFalse();
    List<SyllabusStep> findAllBySyllabusStudySetId(String studySetId);
    List<SyllabusStep> findAllBySyllabusStudySetIdIn(List<String> studySetIds);
    Optional<SyllabusStep> findByCmsVideoLessonId(String cmsVideoLessonId);
    List<SyllabusStep> findAllBySyllabusStudySetIdAndDeletedFalse(String studySetId);
}
