package com.lms.videocourse.repository;

import com.lms.videocourse.entity.ConsumedFlashcardProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedFlashcardProgressEventRepository extends JpaRepository<ConsumedFlashcardProgressEvent, String> {
    boolean existsByEventId(String eventId);
}
