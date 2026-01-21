package com.lms.flashcard.repository;

import com.lms.flashcard.entity.UserCardProgress;
import com.lms.flashcard.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardProgressRepository extends JpaRepository<UserCardProgress, String> {

    Optional<UserCardProgress> findByUserIdAndCardId(String userId, String cardId);

    List<UserCardProgress> findByUserIdAndCardStudySetId(String userId, String studySetId);

    @Query("SELECT COUNT(ucp) FROM UserCardProgress ucp WHERE ucp.userId = :userId AND ucp.card.studySet.id = :studySetId AND ucp.status = :status")
    long countByUserIdAndStudySetIdAndStatus(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId,
            @Param("status") CardStatus status
    );

    @Query("SELECT ucp FROM UserCardProgress ucp WHERE ucp.userId = :userId AND ucp.card.studySet.id = :studySetId AND ucp.status = :status")
    List<UserCardProgress> findByUserIdAndStudySetIdAndStatus(
            @Param("userId") String userId,
            @Param("studySetId") String studySetId,
            @Param("status") CardStatus status
    );
}
