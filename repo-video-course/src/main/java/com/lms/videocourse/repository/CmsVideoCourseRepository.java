package com.lms.videocourse.repository;

import com.lms.videocourse.entity.CmsVideoCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CmsVideoCourseRepository extends JpaRepository<CmsVideoCourse, String> {
    Optional<CmsVideoCourse> findByCmsCourseId(String cmsCourseId);
    List<CmsVideoCourse> findAllByDeletedFalse();
    List<CmsVideoCourse> findAllBySyllabusIdIsNotNullAndDeletedFalse();
}
