package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyllabusFolderRepository extends JpaRepository<SyllabusFolder, String> {
    List<SyllabusFolder> findAllBySyllabusPackageIdAndDeletedFalse(String syllabusPackageId);
    List<SyllabusFolder> findAllByDeletedFalse();
}
