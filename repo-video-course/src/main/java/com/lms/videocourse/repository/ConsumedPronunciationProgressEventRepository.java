package com.lms.videocourse.repository;

import com.lms.videocourse.entity.ConsumedPronunciationProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedPronunciationProgressEventRepository extends JpaRepository<ConsumedPronunciationProgressEvent, String> {
    boolean existsByEventId(String eventId);
}
