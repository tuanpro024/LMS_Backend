package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiLessonQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanjiLessonQuestionRepository extends JpaRepository<KanjiLessonQuestion, String> {
    List<KanjiLessonQuestion> findByKanjiLessonIdAndDeletedFalse(String kanjiLessonId);
    List<KanjiLessonQuestion> findByKanjiLessonIdAndDeletedFalseOrderByContentIndex(String kanjiLessonId);
    List<KanjiLessonQuestion> findByKanjiQuestionIdAndDeletedFalse(String kanjiQuestionId);
    boolean existsByKanjiLessonIdAndKanjiQuestionIdAndDeletedFalse(String lessonId, String questionId);
    boolean existsByKanjiLessonIdAndContentIndexAndDeletedFalse(String lessonId, Integer contentIndex);
    boolean existsByKanjiLessonIdAndContentIndexAndIdNotAndDeletedFalse(String lessonId, Integer contentIndex, String id);
}
