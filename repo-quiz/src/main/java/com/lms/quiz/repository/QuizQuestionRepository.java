package com.lms.quiz.repository;

import com.lms.quiz.entity.QuizQuestion;
import com.lms.quiz.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, String> {

    List<QuizQuestion> findByQuizIdOrderByQuestionIndexAsc(String quizId);

    List<QuizQuestion> findByQuizIdAndQuestionType(String quizId, QuestionType type);

    long countByQuizId(String quizId);

    @Query("SELECT MAX(q.questionIndex) FROM QuizQuestion q WHERE q.quiz.id = :quizId")
    Optional<Integer> findMaxQuestionIndexByQuizId(@Param("quizId") String quizId);
}
