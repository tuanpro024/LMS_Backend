package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusActivityRepository extends JpaRepository<SyllabusActivity, String> {
    List<SyllabusActivity> findAllBySyllabusStepIdAndDeletedFalse(String syllabusStepId);
    List<SyllabusActivity> findAllByDeletedFalse();
    Optional<SyllabusActivity> findByCmsModuleId(String cmsModuleId);
    List<SyllabusActivity> findAllBySyllabusStepIdInAndDeletedFalse(List<String> stepIds);
}
