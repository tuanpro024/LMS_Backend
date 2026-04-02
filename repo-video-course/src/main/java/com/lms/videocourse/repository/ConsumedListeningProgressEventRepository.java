package com.lms.videocourse.repository;

import com.lms.videocourse.entity.ConsumedListeningProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedListeningProgressEventRepository extends JpaRepository<ConsumedListeningProgressEvent, String> {
}
