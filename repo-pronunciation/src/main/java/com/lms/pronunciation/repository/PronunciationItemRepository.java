package com.lms.pronunciation.repository;

import com.lms.pronunciation.entity.PronunciationItem;
import com.lms.pronunciation.entity.enums.PronunciationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PronunciationItemRepository extends JpaRepository<PronunciationItem, String> {

    List<PronunciationItem> findByStudySetIdAndDeletedFalse(String studySetId);

    List<PronunciationItem> findByStudySetIdAndTypeAndDeletedFalse(String studySetId, PronunciationType type);

    Page<PronunciationItem> findByStudySetIdAndDeletedFalse(String studySetId, Pageable pageable);

    boolean existsByStudySetIdAndSymbolAndDeletedFalse(String studySetId, String symbol);

    boolean existsByStudySetIdAndContentIndexAndDeletedFalse(String studySetId, Integer contentIndex);

    boolean existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(String studySetId, Integer contentIndex, String id);

    @Query("SELECT p FROM PronunciationItem p WHERE p.deleted = false " +
           "AND (:studySetId IS NULL OR p.studySet.id = :studySetId) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:keyword IS NULL OR LOWER(p.symbol) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.pinyin) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PronunciationItem> search(@Param("studySetId") String studySetId,
                                   @Param("type") PronunciationType type,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    @Query("SELECT p FROM PronunciationItem p WHERE p.deleted = false " +
           "AND (:studySetId IS NULL OR p.studySet.id = :studySetId) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:keyword IS NULL OR LOWER(p.symbol) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.pinyin) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<PronunciationItem> searchList(@Param("studySetId") String studySetId,
                                       @Param("type") PronunciationType type,
                                       @Param("keyword") String keyword);

    @Query("SELECT MAX(p.contentIndex) FROM PronunciationItem p WHERE p.studySet.id = :studySetId AND p.deleted = false")
    Integer findMaxContentIndexByStudySetId(@Param("studySetId") String studySetId);

    long countByStudySetIdAndDeletedFalse(String studySetId);

    void deleteByStudySetId(String studySetId);
}
