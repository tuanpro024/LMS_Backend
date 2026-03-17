package com.lms.onllearning.repository;

import com.lms.onllearning.entity.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusRepository extends JpaRepository<Syllabus, String> {
    List<Syllabus> findAllByDeletedFalseOrderByCreatedAtDesc();
}
