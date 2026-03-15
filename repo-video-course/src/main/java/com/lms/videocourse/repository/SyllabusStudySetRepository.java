package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusStudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyllabusStudySetRepository extends JpaRepository<SyllabusStudySet, String> {
    List<SyllabusStudySet> findAllBySyllabusFolderIdAndDeletedFalse(String syllabusFolderId);
    List<SyllabusStudySet> findAllByDeletedFalse();
}
