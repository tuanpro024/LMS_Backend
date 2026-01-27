package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanjiLessonRepository extends JpaRepository<KanjiLesson, String> {
    List<KanjiLesson> findByDeletedFalse();
    List<KanjiLesson> findByStudySetIdAndDeletedFalse(String studySetId);
}
