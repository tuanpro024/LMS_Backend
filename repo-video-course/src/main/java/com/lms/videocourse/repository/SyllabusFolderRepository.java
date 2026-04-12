package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusFolderRepository extends JpaRepository<SyllabusFolder, String> {
    List<SyllabusFolder> findAllBySyllabusPackageIdAndDeletedFalse(String syllabusPackageId);
    List<SyllabusFolder> findAllByDeletedFalse();
    Optional<SyllabusFolder> findByCmsFolderId(String cmsFolderId);
}
