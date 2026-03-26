package com.lms.learningpath.repository;

import com.lms.learningpath.entity.ConsumedWritingProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedWritingProgressEventRepository extends JpaRepository<ConsumedWritingProgressEvent, String> {

    boolean existsByEventId(String eventId);
}