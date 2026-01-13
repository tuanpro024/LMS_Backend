package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByStudySetIdAndStatus(String studySetId, CardStatus status);

    long countByStudySetIdAndStatus(String studySetId, CardStatus status);

    long countByStudySetId(String studySetId);
}
