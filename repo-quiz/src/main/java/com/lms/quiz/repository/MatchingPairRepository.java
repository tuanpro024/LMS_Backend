package com.lms.quiz.repository;

import com.lms.quiz.entity.MatchingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingPairRepository extends JpaRepository<MatchingPair, String> {

    List<MatchingPair> findByQuestionId(String questionId);
}
