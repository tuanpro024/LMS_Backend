package com.lms.onllearning.repository;

import com.lms.onllearning.entity.OnlineCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OnlineCourseRepository extends JpaRepository<OnlineCourse, String> {
    List<OnlineCourse> findAllByDeletedFalseOrderByCreatedAtDesc();

    Optional<OnlineCourse> findByIdAndDeletedFalse(String id);

    @Modifying
    @Query("UPDATE OnlineCourse c SET c.deleted = true WHERE c.cmsSynced = true AND c.deleted = false AND c.id NOT IN :cmsIds")
    void softDeleteCmsCoursesNotIn(@Param("cmsIds") List<String> cmsIds);

    @Modifying
    @Query("UPDATE OnlineCourse c SET c.deleted = true WHERE c.cmsSynced = true AND c.deleted = false")
    void softDeleteAllCmsCourses();
}
