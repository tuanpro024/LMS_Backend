package com.lms.quiz.repository;

import com.lms.quiz.entity.QuizBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizBlankRepository extends JpaRepository<QuizBlank, String> {

    List<QuizBlank> findByQuestionIdOrderByBlankIndexAsc(String questionId);
}
