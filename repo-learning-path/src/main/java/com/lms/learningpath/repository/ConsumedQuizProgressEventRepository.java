package com.lms.learningpath.repository;

import com.lms.learningpath.entity.ConsumedQuizProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedQuizProgressEventRepository extends JpaRepository<ConsumedQuizProgressEvent, String> {

    boolean existsByEventId(String eventId);
}
