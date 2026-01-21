package com.lms.writing.repository;

import com.lms.writing.entity.Word;
import com.lms.writing.entity.enums.WordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, String> {
    List<Word> findByStudySetId(String studySetId);

    List<Word> findByStudySetIdOrderByIdAsc(String studySetId);

    List<Word> findByStudySetIdAndStatus(String studySetId, WordStatus status);

    long countByStudySetIdAndStatus(String studySetId, WordStatus status);

    void deleteByStudySetId(String studySetId);
}
