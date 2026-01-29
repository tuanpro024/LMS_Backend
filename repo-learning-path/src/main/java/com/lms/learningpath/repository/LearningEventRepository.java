package com.lms.learningpath.repository;

import com.lms.learningpath.entity.LearningEvent;
import com.lms.learningpath.entity.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LearningEventRepository extends JpaRepository<LearningEvent, String> {

    Page<LearningEvent> findByUserIdOrderByOccurredAtDesc(String userId, Pageable pageable);

    List<LearningEvent> findByUserIdAndOccurredAtBetween(
            String userId,
            Instant start,
            Instant end
    );

    List<LearningEvent> findByUserIdAndEventType(String userId, EventType eventType);

    @Query("SELECT COUNT(e) FROM LearningEvent e " +
            "WHERE e.userId = :userId " +
            "AND e.eventType = :eventType " +
            "AND e.occurredAt BETWEEN :start AND :end")
    long countByUserIdAndEventTypeAndOccurredAtBetween(
            @Param("userId") String userId,
            @Param("eventType") EventType eventType,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT SUM(e.durationSeconds) FROM LearningEvent e " +
            "WHERE e.userId = :userId " +
            "AND e.occurredAt BETWEEN :start AND :end " +
            "AND e.durationSeconds IS NOT NULL")
    Long sumDurationByUserAndPeriod(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT e.eventType, COUNT(e) FROM LearningEvent e " +
            "WHERE e.userId = :userId " +
            "AND e.occurredAt BETWEEN :start AND :end " +
            "GROUP BY e.eventType")
    List<Object[]> countEventTypesByUserAndPeriod(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT DATE(e.occurredAt), COUNT(DISTINCT e.userId) FROM LearningEvent e " +
            "WHERE e.occurredAt BETWEEN :start AND :end " +
            "AND e.eventType = 'SESSION_START' " +
            "GROUP BY DATE(e.occurredAt) " +
            "ORDER BY DATE(e.occurredAt)")
    List<Object[]> countDailyActiveUsers(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}