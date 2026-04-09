package com.lms.aipractice.repository;

import com.lms.aipractice.entity.AiPracticeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPracticeAnswerRepository extends JpaRepository<AiPracticeAnswer, String> {

    List<AiPracticeAnswer> findByAttemptIdAndDeletedFalse(String attemptId);

    boolean existsByAttemptIdAndDeletedFalse(String attemptId);

    Optional<AiPracticeAnswer> findByAttemptIdAndItemIdAndDeletedFalse(String attemptId, String itemId);

    boolean existsByAttemptIdAndItemIdAndDeletedFalse(String attemptId, String itemId);
}
