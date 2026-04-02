package com.lms.videocourse.repository;

import com.lms.videocourse.entity.ConsumedKanjiProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedKanjiProgressEventRepository extends JpaRepository<ConsumedKanjiProgressEvent, String> {
}
