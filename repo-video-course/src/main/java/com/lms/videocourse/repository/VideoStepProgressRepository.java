package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoStepProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoStepProgressRepository extends JpaRepository<VideoStepProgress, String> {

    Optional<VideoStepProgress> findByUserIdAndStepId(String userId, String stepId);

    List<VideoStepProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<VideoStepProgress> findByUserIdAndStepIdIn(String userId, List<String> stepIds);
}
