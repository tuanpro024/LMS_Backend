package com.lms.pronunciation.repository;

import com.lms.pronunciation.entity.UserPronunciationItemProgress;
import com.lms.pronunciation.entity.enums.PronunciationLearningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPronunciationItemProgressRepository extends JpaRepository<UserPronunciationItemProgress, String> {

    Optional<UserPronunciationItemProgress> findByUserIdAndPronunciationItemId(String userId, String pronunciationItemId);

    @Query("SELECT COUNT(p) FROM UserPronunciationItemProgress p " +
           "WHERE p.userId = :userId AND p.pronunciationItem.studySet.id = :studySetId " +
           "AND p.status = :status")
    long countByUserIdAndStudySetIdAndStatus(String userId, String studySetId, PronunciationLearningStatus status);

    @Query("SELECT p FROM UserPronunciationItemProgress p " +
           "WHERE p.userId = :userId AND p.pronunciationItem.studySet.id = :studySetId")
    java.util.List<UserPronunciationItemProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    void deleteByPronunciationItemStudySetId(String studySetId);
}
