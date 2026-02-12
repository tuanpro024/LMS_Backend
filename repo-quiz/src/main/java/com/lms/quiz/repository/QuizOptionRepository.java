package com.lms.quiz.repository;

import com.lms.quiz.entity.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, String> {

    List<QuizOption> findByQuestionIdOrderByOptionIndexAsc(String questionId);
}
