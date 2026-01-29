package com.lms.learningpath.repository;

import com.lms.learningpath.entity.UserModuleProgress;
import com.lms.learningpath.entity.enums.ModuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserModuleProgressRepository extends JpaRepository<UserModuleProgress, String> {

    Optional<UserModuleProgress> findByUserIdAndModuleId(String userId, String moduleId);

    List<UserModuleProgress> findByUserIdAndModule_StudySetId(String userId, String studySetId);

    @Query("SELECT COUNT(p) FROM UserModuleProgress p " +
            "WHERE p.userId = :userId " +
            "AND p.module.studySetId = :studySetId " +
            "AND p.module.isRequired = true " +
            "AND p.status = :status")
    long countRequiredByStatusAndStudySetId(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId,
            @Param("status") ModuleStatus status
    );

    @Query("SELECT AVG(p.score) FROM UserModuleProgress p " +
            "WHERE p.userId = :userId " +
            "AND p.module.studySetId = :studySetId " +
            "AND p.module.isRequired = true " +
            "AND p.status = 'COMPLETED' " +
            "AND p.score IS NOT NULL")
    Double getAverageScoreForRequiredModules(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId
    );

    @Query("SELECT p FROM UserModuleProgress p " +
            "WHERE p.userId = :userId " +
            "AND p.module.studySetId = :studySetId " +
            "AND p.status = 'COMPLETED' " +
            "ORDER BY p.completedAt DESC")
    List<UserModuleProgress> findCompletedModulesByStudySetId(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId
    );
}