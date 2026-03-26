package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoPracticeModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VideoPracticeModuleProgressRepository extends JpaRepository<VideoPracticeModuleProgress, String> {
    Optional<VideoPracticeModuleProgress> findByUserIdAndVideoModuleId(String userId, String videoModuleId);
    
    List<VideoPracticeModuleProgress> findByUserIdAndStepId(String userId, String stepId);
}
