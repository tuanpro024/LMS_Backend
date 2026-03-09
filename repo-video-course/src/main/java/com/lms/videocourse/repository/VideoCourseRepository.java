package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCourseRepository extends JpaRepository<VideoCourse, String> {

    List<VideoCourse> findByStudySetIdAndIsActiveTrueOrderByCreatedAtAsc(String studySetId);

    List<VideoCourse> findByStudySetId(String studySetId);

    List<VideoCourse> findByIsActiveTrue();
}
