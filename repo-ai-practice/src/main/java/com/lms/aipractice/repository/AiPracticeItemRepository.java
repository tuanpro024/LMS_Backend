package com.lms.aipractice.repository;

import com.lms.aipractice.entity.AiPracticeItem;
import com.lms.aipractice.entity.enums.AiItemSubtype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPracticeItemRepository extends JpaRepository<AiPracticeItem, String> {

    List<AiPracticeItem> findByStudySetIdAndDeletedFalseOrderByContentIndexAsc(String studySetId);

    List<AiPracticeItem> findByStudySetIdAndQuestionSubtypeAndDeletedFalse(String studySetId, AiItemSubtype subtype);

    boolean existsByStudySetIdAndContentIndexAndDeletedFalse(String studySetId, Integer contentIndex);

    boolean existsByStudySetIdAndContentIndexAndIdNotAndDeletedFalse(String studySetId, Integer contentIndex, String id);

    Optional<AiPracticeItem> findByIdAndDeletedFalse(String id);


    @Query("SELECT MAX(a.contentIndex) FROM AiPracticeItem a WHERE a.studySet.id = :studySetId AND a.deleted = false")
    Integer findMaxContentIndexByStudySetId(@Param("studySetId") String studySetId);

    void deleteByStudySetId(String studySetId);
}
