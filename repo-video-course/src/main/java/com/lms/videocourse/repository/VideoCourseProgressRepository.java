package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoCourseProgressRepository extends JpaRepository<VideoCourseProgress, String> {

    Optional<VideoCourseProgress> findByUserIdAndVideoCourseId(String userId, String videoCourseId);

    List<VideoCourseProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<VideoCourseProgress> findByUserId(String userId);
}
