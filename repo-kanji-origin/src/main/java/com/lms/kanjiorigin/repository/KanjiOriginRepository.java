package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiOrigin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanjiOriginRepository extends JpaRepository<KanjiOrigin, String> {
    List<KanjiOrigin> findByKanjiLessonIdAndDeletedFalse(String kanjiLessonId);
    boolean existsByTermAndKanjiLessonIdAndDeletedFalse(String term, String kanjiLessonId);
    boolean existsByKanjiLessonIdAndContentIndexAndDeletedFalse(String kanjiLessonId, Integer contentIndex);
    boolean existsByKanjiLessonIdAndContentIndexAndIdNotAndDeletedFalse(String kanjiLessonId, Integer contentIndex, String id);
    
    Page<KanjiOrigin> findByKanjiLessonIdAndDeletedFalse(String kanjiLessonId, Pageable pageable);
    
    @Query("SELECT o FROM KanjiOrigin o WHERE o.deleted = false " +
           "AND (:lessonId IS NULL OR o.kanjiLesson.id = :lessonId) " +
           "AND (:keyword IS NULL OR LOWER(o.term) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KanjiOrigin> search(@Param("lessonId") String lessonId, 
                             @Param("keyword") String keyword, 
                             Pageable pageable);
    
    @Query("SELECT o FROM KanjiOrigin o WHERE o.deleted = false " +
           "AND (:lessonId IS NULL OR o.kanjiLesson.id = :lessonId) " +
           "AND (:keyword IS NULL OR LOWER(o.term) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KanjiOrigin> searchList(@Param("lessonId") String lessonId, 
                                  @Param("keyword") String keyword);
    
    // For import
    Optional<KanjiOrigin> findByKanjiLessonIdAndTermAndDeletedFalse(String kanjiLessonId, String term);
    
    @Query("SELECT MAX(o.contentIndex) FROM KanjiOrigin o WHERE o.kanjiLesson.id = :lessonId AND o.deleted = false")
    Integer findMaxContentIndexByKanjiLessonId(@Param("lessonId") String lessonId);
}
