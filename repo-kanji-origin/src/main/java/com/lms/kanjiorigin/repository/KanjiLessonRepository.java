package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiLesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanjiLessonRepository extends JpaRepository<KanjiLesson, String> {
    List<KanjiLesson> findByDeletedFalse();
    Optional<KanjiLesson> findByIdAndDeletedFalse(String id);
    List<KanjiLesson> findByStudySetIdAndDeletedFalse(String studySetId);

    Page<KanjiLesson> findByDeletedFalse(Pageable pageable);
    
    Page<KanjiLesson> findByStudySetIdAndDeletedFalse(String studySetId, Pageable pageable);
    
    @Query("SELECT l FROM KanjiLesson l WHERE l.deleted = false " +
           "AND (:studySetId IS NULL OR l.studySet.id = :studySetId) " +
           "AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KanjiLesson> search(@Param("studySetId") String studySetId, 
                             @Param("keyword") String keyword, 
                             Pageable pageable);
    
    @Query("SELECT l FROM KanjiLesson l WHERE l.deleted = false " +
           "AND (:studySetId IS NULL OR l.studySet.id = :studySetId) " +
           "AND (:keyword IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KanjiLesson> searchList(@Param("studySetId") String studySetId, 
                                  @Param("keyword") String keyword);
    boolean existsByStudySetIdAndContentIndex(String studySetId, Integer contentIndex);
    boolean existsByStudySetIdAndContentIndexAndIdNot(String studySetId, Integer contentIndex, String id);
    boolean existsByStudySetIdAndTitle(String studySetId, String title);
    boolean existsByStudySetIdAndTitleAndIdNot(String studySetId, String title, String id);
    boolean existsByIdAndDeletedFalse(String id);
}
