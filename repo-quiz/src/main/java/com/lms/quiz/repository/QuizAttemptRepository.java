package com.lms.quiz.repository;

import com.lms.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, String> {

    long countByUserIdAndQuizId(String userId, String quizId);

    void deleteByStudySetId(String studySetId);
}
