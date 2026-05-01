package com.lms.quiz.repository;

import com.lms.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, String> {

    List<Quiz> findByStudySetIdOrderByContentIndexAsc(String studySetId);

    @Query("SELECT q FROM Quiz q WHERE q.studySet.id = :studySetId")
    List<Quiz> findActiveByStudySetId(String studySetId);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.studySet.id = :studySetId")
    long countByStudySetId(String studySetId);

    void deleteByStudySetId(String studySetId);
}
