package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoStepUnlockRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoStepUnlockRuleRepository extends JpaRepository<VideoStepUnlockRule, String> {

    Optional<VideoStepUnlockRule> findByStepIdAndIsActiveTrue(String stepId);

    void deleteByStepId(String stepId);
}
