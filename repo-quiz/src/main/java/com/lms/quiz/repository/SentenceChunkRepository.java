package com.lms.quiz.repository;

import com.lms.quiz.entity.SentenceChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceChunkRepository extends JpaRepository<SentenceChunk, String> {

    List<SentenceChunk> findByQuestionIdOrderByCorrectPositionAsc(String questionId);
}
