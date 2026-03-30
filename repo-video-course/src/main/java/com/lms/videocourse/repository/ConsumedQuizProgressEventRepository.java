package com.lms.videocourse.repository;

import com.lms.videocourse.entity.ConsumedQuizProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedQuizProgressEventRepository extends JpaRepository<ConsumedQuizProgressEvent, String> {
}
