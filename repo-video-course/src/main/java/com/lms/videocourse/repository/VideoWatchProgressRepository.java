package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoWatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoWatchProgressRepository extends JpaRepository<VideoWatchProgress, String> {

    Optional<VideoWatchProgress> findByUserIdAndVideoModuleId(String userId, String videoModuleId);

    List<VideoWatchProgress> findByUserIdAndStepId(String userId, String stepId);

    List<VideoWatchProgress> findByUserIdAndVideoModuleIdIn(String userId, List<String> moduleIds);
}
