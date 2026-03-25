package com.lms.learningpath.repository;

import com.lms.learningpath.entity.ConsumedFlashcardProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedFlashcardProgressEventRepository
        extends JpaRepository<ConsumedFlashcardProgressEvent, String> {

    boolean existsByEventId(String eventId);
}
