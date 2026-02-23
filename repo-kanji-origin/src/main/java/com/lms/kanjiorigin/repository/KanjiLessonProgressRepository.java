package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanjiLessonProgressRepository extends JpaRepository<KanjiLessonProgress, Long> {
    Optional<KanjiLessonProgress> findByUserIdAndKanjiLesson_Id(String userId, String kanjiLessonId);

    List<KanjiLessonProgress> findByUserId(String userId);
}
