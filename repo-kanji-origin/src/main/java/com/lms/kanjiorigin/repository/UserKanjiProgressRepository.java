package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.UserKanjiProgress;
import com.lms.kanjiorigin.entity.enums.KanjiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserKanjiProgressRepository extends JpaRepository<UserKanjiProgress, String> {

    Optional<UserKanjiProgress> findByUserIdAndKanjiOriginId(String userId, String kanjiOriginId);

    @Query("SELECT COUNT(ukp) FROM UserKanjiProgress ukp " +
            "WHERE ukp.userId = :userId " +
            "AND ukp.kanjiOrigin.studySet.id = :studySetId " +
            "AND ukp.status = :status")
    long countByUserIdAndStudySetIdAndStatus(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId,
            @Param("status") KanjiStatus status);

    @Query("SELECT ukp FROM UserKanjiProgress ukp " +
            "WHERE ukp.userId = :userId " +
            "AND ukp.kanjiOrigin.studySet.id = :studySetId")
    List<UserKanjiProgress> findByUserIdAndStudySetId(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId);

    void deleteByKanjiOriginId(String kanjiOriginId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM UserKanjiProgress ukp WHERE ukp.kanjiOrigin.id IN (SELECT k.id FROM KanjiOrigin k WHERE k.studySet.id = :studySetId)")
    void deleteByStudySetId(@Param("studySetId") String studySetId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM UserKanjiProgress p WHERE p.deleted = true")
    void purgeSoftDeleted();
}