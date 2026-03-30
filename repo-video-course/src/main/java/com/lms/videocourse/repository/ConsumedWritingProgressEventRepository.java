package com.lms.videocourse.repository;

import com.lms.videocourse.entity.ConsumedWritingProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedWritingProgressEventRepository extends JpaRepository<ConsumedWritingProgressEvent, String> {
}
