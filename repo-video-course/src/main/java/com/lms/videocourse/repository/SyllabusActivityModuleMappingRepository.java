package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusActivityModuleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusActivityModuleMappingRepository extends JpaRepository<SyllabusActivityModuleMapping, String> {
    Optional<SyllabusActivityModuleMapping> findBySyllabusActivityIdAndDeletedFalse(String syllabusActivityId);
    List<SyllabusActivityModuleMapping> findAllBySyllabusActivityIdInAndDeletedFalse(List<String> activityIds);
    List<SyllabusActivityModuleMapping> findAllByDeletedFalse();
}
