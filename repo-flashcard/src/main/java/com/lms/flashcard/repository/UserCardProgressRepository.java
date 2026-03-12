package com.lms.flashcard.repository;

import com.lms.flashcard.entity.UserCardProgress;
import com.lms.flashcard.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCardProgressRepository extends JpaRepository<UserCardProgress, String> {

        Optional<UserCardProgress> findByUserIdAndCardId(String userId, String cardId);

        @Query("SELECT ucp.card.id FROM UserCardProgress ucp WHERE ucp.userId = :userId AND ucp.card.studySet.id = :studySetId AND ucp.status = :status")
        List<String> findCardIdsByUserIdAndStudySetIdAndStatus(@Param("userId") String userId,
                        @Param("studySetId") String studySetId, @Param("status") CardStatus status);

        @Query("SELECT COUNT(ucp) FROM UserCardProgress ucp WHERE ucp.userId = :userId AND ucp.card.studySet.id = :studySetId AND ucp.status = :status")
        long countByUserIdAndStudySetIdAndStatus(@Param("userId") String userId, @Param("studySetId") String studySetId,
                        @Param("status") CardStatus status);

        void deleteByCardId(String cardId);
}
