package com.lms.quiz.repository;

import com.lms.quiz.entity.UserQuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuizProgressRepository extends JpaRepository<UserQuizProgress, String> {

    Optional<UserQuizProgress> findByUserIdAndQuizId(String userId, String quizId);

    long countByUserIdAndStudySetIdAndCompletedTrue(String userId, String studySetId);

    void deleteByStudySetId(String studySetId);
}
