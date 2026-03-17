package com.lms.onllearning.repository;

import com.lms.onllearning.entity.OnlineCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnlineCourseRepository extends JpaRepository<OnlineCourse, String> {
    List<OnlineCourse> findAllByDeletedFalseOrderByCreatedAtDesc();
}
