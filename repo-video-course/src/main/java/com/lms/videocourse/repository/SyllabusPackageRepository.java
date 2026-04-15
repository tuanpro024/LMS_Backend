package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusPackageRepository extends JpaRepository<SyllabusPackage, String> {
    List<SyllabusPackage> findAllByDeletedFalse();
    Optional<SyllabusPackage> findByCmsSyllabusIdAndCmsCourseId(String cmsSyllabusId, String cmsCourseId);
    List<SyllabusPackage> findAllByCmsSyllabusIdIsNotNullAndDeletedFalse();
}
