package com.lms.aipractice.repository;

import com.lms.aipractice.entity.AiGradingJob;
import com.lms.aipractice.entity.enums.GradingJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiGradingJobRepository extends JpaRepository<AiGradingJob, String> {

    Optional<AiGradingJob> findTopByAnswerIdAndDeletedFalseOrderByCreatedAtDesc(String answerId);

    Optional<AiGradingJob> findTopByAnswerIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            String answerId, GradingJobStatus status);

    List<AiGradingJob> findByAttemptIdAndDeletedFalse(String attemptId);

    /** Used by polling service to find jobs still waiting for HSK_API result */
    List<AiGradingJob> findByProviderAndStatusInAndDeletedFalse(
            String provider, List<GradingJobStatus> statuses);

    Optional<AiGradingJob> findByIdAndDeletedFalse(String id);
}
