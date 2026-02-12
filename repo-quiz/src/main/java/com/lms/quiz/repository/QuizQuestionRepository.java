package com.lms.quiz.repository;

import com.lms.quiz.entity.QuizQuestion;
import com.lms.quiz.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, String> {

    List<QuizQuestion> findByQuizIdOrderByQuestionIndexAsc(String quizId);

    List<QuizQuestion> findByQuizIdAndQuestionType(String quizId, QuestionType type);

    long countByQuizId(String quizId);
}
