package com.lms.flashcard.repository;

import com.lms.flashcard.entity.FlashcardStudySetProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardStudySetProgressRepository extends JpaRepository<FlashcardStudySetProgress, String> {
    Optional<FlashcardStudySetProgress> findByUserIdAndStudySetId(String userId, String studySetId);
}
