package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanjiLessonQuestionRepository extends JpaRepository<KanjiLessonQuestion, String> {
    List<KanjiLessonQuestion> findByKanjiLessonIdAndDeletedFalse(String kanjiLessonId);
}
