package com.lms.quiz.repository;

import com.lms.quiz.entity.UserQuizStudySetProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizStudySetProgressRepository extends JpaRepository<UserQuizStudySetProgress, String> {

    Optional<UserQuizStudySetProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<UserQuizStudySetProgress> findByUserId(String userId);
}
