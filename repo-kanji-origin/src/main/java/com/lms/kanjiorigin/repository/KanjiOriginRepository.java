package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiOrigin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanjiOriginRepository extends JpaRepository<KanjiOrigin, String> {
    List<KanjiOrigin> findByStudySetIdOrderByContentIndexAsc(String studySetId);

    boolean existsByTermAndStudySetId(String term, String studySetId);

    boolean existsByStudySetIdAndContentIndex(String studySetId, Integer contentIndex);

    boolean existsByStudySetIdAndContentIndexAndIdNot(String studySetId, Integer contentIndex,
            String id);

    long countByStudySetId(String studySetId);

    @Query("SELECT o FROM KanjiOrigin o WHERE " +
           "(:studySetId IS NULL OR o.studySet.id = :studySetId) " +
           "AND (:keyword IS NULL OR LOWER(o.term) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.pinyin) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.sinoVn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KanjiOrigin> search(@Param("studySetId") String studySetId,
                             @Param("keyword") String keyword, 
                             Pageable pageable);

    @Query("SELECT o FROM KanjiOrigin o WHERE " +
           "(:studySetId IS NULL OR o.studySet.id = :studySetId) " +
           "AND (:keyword IS NULL OR LOWER(o.term) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.pinyin) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.sinoVn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KanjiOrigin> searchList(@Param("studySetId") String studySetId,
                                  @Param("keyword") String keyword);

    // For import
    Optional<KanjiOrigin> findByStudySetIdAndTerm(String studySetId, String term);

    @Query("SELECT MAX(o.contentIndex) FROM KanjiOrigin o WHERE o.studySet.id = :studySetId")
    Integer findMaxContentIndexByStudySetId(@Param("studySetId") String studySetId);

    void deleteByStudySetId(String studySetId);
    
    @Modifying
    @Query("DELETE FROM KanjiOrigin o WHERE o.deleted = true")
    void purgeSoftDeleted();
}
