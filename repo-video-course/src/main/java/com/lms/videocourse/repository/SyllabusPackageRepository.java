package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyllabusPackageRepository extends JpaRepository<SyllabusPackage, String> {
    List<SyllabusPackage> findAllByDeletedFalse();
}
