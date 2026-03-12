package com.lms.writing.repository;

import com.lms.writing.entity.Word;
import com.lms.writing.entity.enums.WordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, String> {

    Optional<Word> findByIdAndDeletedFalse(String id);

    List<Word> findByStudySetIdAndDeletedFalse(String studySetId);

    List<Word> findByStudySetIdAndDeletedFalseOrderByIdAsc(String studySetId);

    long countByStudySetIdAndDeletedFalse(String studySetId);

    List<Word> findByStudySetId(String studySetId);

    List<Word> findByStudySetIdOrderByIdAsc(String studySetId);

    long countByStudySetId(String studySetId);

    void deleteByStudySetId(String studySetId);
}
