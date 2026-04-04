package com.lms.aipractice.repository;

import com.lms.aipractice.entity.AiGradingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiGradingResultRepository extends JpaRepository<AiGradingResult, String> {

    Optional<AiGradingResult> findByGradingJobIdAndDeletedFalse(String gradingJobId);

    List<AiGradingResult> findByGradingJobIdInAndDeletedFalse(List<String> gradingJobIds);
}
