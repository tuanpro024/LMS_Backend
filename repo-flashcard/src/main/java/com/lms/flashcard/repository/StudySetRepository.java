package com.lms.flashcard.repository;

import com.lms.flashcard.entity.StudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudySetRepository extends JpaRepository<StudySet, String> {

    List<StudySet> findByUserId(String userId);

    List<StudySet> findByIsPrivateFalse();

    List<StudySet> findByTitleContainingIgnoreCaseAndIsPrivateFalse(String title);

    List<StudySet> findByUserIdAndTitleContainingIgnoreCase(String userId, String title);
}
