package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanjiOriginRepository extends JpaRepository<KanjiOrigin, String> {
    List<KanjiOrigin> findByKanjiLessonIdAndDeletedFalse(String kanjiLessonId);
    boolean existsByTermAndKanjiLessonIdAndDeletedFalse(String term, String kanjiLessonId);
}
