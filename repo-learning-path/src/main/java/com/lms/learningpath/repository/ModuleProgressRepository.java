package com.lms.learningpath.repository;

import com.lms.learningpath.entity.ModuleProgress;
import com.lms.learningpath.entity.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ModuleProgress (step module progress tracking).
 * Updated for step-based hierarchy.
 */
@Repository
public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, String> {

        Optional<ModuleProgress> findByUserIdAndStepModuleId(String userId, String stepModuleId);

        List<ModuleProgress> findByUserIdAndStepModuleIdIn(String userId, List<String> stepModuleIds);

        List<ModuleProgress> findByUserIdAndStepId(String userId, String stepId);

        List<ModuleProgress> findByUserId(String userId);

        @Query("SELECT COUNT(mp) FROM ModuleProgress mp WHERE mp.userId = :userId " +
                        "AND mp.stepId = :stepId AND mp.status = :status")
        long countByUserIdAndStepIdAndStatus(@Param("userId") String userId,
                        @Param("stepId") String stepId,
                        @Param("status") ProgressStatus status);

        @Query("SELECT mp FROM ModuleProgress mp WHERE mp.userId = :userId " +
                        "AND mp.stepModuleId IN :moduleIds")
        List<ModuleProgress> findByUserIdAndModuleIds(@Param("userId") String userId,
                        @Param("moduleIds") List<String> moduleIds);

        void deleteByStepId(String stepId);
}
