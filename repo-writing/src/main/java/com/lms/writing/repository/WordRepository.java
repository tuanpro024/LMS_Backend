package com.lms.writing.repository;

import com.lms.writing.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, String> {
    List<Word> findByStudySetId(String studySetId);

    List<Word> findByStudySetIdOrderByIdAsc(String studySetId);

    void deleteByStudySetId(String studySetId);
}
