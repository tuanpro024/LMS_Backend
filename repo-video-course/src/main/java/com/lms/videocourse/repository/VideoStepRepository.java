package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoStepRepository extends JpaRepository<VideoStep, String> {

    List<VideoStep> findByVideoCourseIdAndIsActiveTrueOrderByStepOrderAsc(String videoCourseId);

    List<VideoStep> findByVideoCourseIdOrderByStepOrderAsc(String videoCourseId);

    long countByVideoCourseIdAndIsActiveTrue(String videoCourseId);
}
