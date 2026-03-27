package com.lms.learningpath.repository;

import com.lms.learningpath.entity.ConsumedKanjiProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedKanjiProgressEventRepository extends JpaRepository<ConsumedKanjiProgressEvent, String> {

    boolean existsByEventId(String eventId);
}
