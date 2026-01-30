package com.lms.learningpath.repository;

import com.lms.learningpath.entity.ModuleProgress;
import com.lms.learningpath.entity.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, String> {

    Optional<ModuleProgress> findByUserIdAndStudySetModuleId(String userId, String studySetModuleId);

    List<ModuleProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<ModuleProgress> findByUserId(String userId);

    @Query("SELECT COUNT(mp) FROM ModuleProgress mp WHERE mp.userId = :userId " +
            "AND mp.studySetId = :studySetId AND mp.status = :status")
    long countByUserIdAndStudySetIdAndStatus(@Param("userId") String userId,
            @Param("studySetId") String studySetId,
            @Param("status") ProgressStatus status);

    @Query("SELECT mp FROM ModuleProgress mp WHERE mp.userId = :userId " +
            "AND mp.studySetModuleId IN :moduleIds")
    List<ModuleProgress> findByUserIdAndModuleIds(@Param("userId") String userId,
            @Param("moduleIds") List<String> moduleIds);

    void deleteByStudySetId(String studySetId);
}
