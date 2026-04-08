package com.lms.aipractice.repository;

import com.lms.aipractice.entity.AiPracticeAttempt;
import com.lms.aipractice.entity.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPracticeAttemptRepository extends JpaRepository<AiPracticeAttempt, String> {

    List<AiPracticeAttempt> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(String userId);

    List<AiPracticeAttempt> findByStudySetIdAndDeletedFalse(String studySetId);

    Optional<AiPracticeAttempt> findByIdAndDeletedFalse(String id);

    Optional<AiPracticeAttempt> findByUserIdAndStudySetIdAndStatusAndDeletedFalse(
            String userId, String studySetId, AttemptStatus status);

    List<AiPracticeAttempt> findByUserIdAndStudySetIdInAndDeletedFalseOrderByCreatedAtDesc(
            String userId, List<String> studySetIds);
}
