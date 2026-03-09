package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Card;
import com.lms.flashcard.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    Optional<Card> findByIdAndDeletedFalse(String id);

    List<Card> findByStudySetIdAndDeletedFalse(String studySetId);

    long countByStudySetIdAndDeletedFalse(String studySetId);

    List<Card> findByStudySetId(String studySetId);

    long countByStudySetId(String studySetId);
}
