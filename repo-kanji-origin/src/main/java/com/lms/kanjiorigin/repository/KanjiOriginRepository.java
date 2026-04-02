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
    List<KanjiOrigin> findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(String studySetId);

    boolean existsByTermAndStudySetIdAndDeletedFalse(String term, String studySetId);

    boolean existsByStudySetIdAndContentIndexAndDeletedFalse(String studySetId, Integer contentIndex);

    boolean existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(String studySetId, Integer contentIndex,
            String id);

    long countByStudySetIdAndDeletedFalse(String studySetId);

    @Query("SELECT o FROM KanjiOrigin o WHERE o.deleted = false " +
           "AND (:studySetId IS NULL OR o.studySet.id = :studySetId) " +
           "AND (:keyword IS NULL OR LOWER(o.term) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KanjiOrigin> search(@Param("studySetId") String studySetId,
                             @Param("keyword") String keyword, 
                             Pageable pageable);

    @Query("SELECT o FROM KanjiOrigin o WHERE o.deleted = false " +
           "AND (:studySetId IS NULL OR o.studySet.id = :studySetId) " +
           "AND (:keyword IS NULL OR LOWER(o.term) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KanjiOrigin> searchList(@Param("studySetId") String studySetId,
                                  @Param("keyword") String keyword);

    // For import
    Optional<KanjiOrigin> findByStudySetIdAndTermAndDeletedFalse(String studySetId, String term);

    @Query("SELECT MAX(o.contentIndex) FROM KanjiOrigin o WHERE o.studySet.id = :studySetId AND o.deleted = false")
    Integer findMaxContentIndexByStudySetId(@Param("studySetId") String studySetId);
}
