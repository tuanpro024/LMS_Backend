package com.lms.identity.repository;

import com.lms.common.dto.InteractionEventType;
import com.lms.identity.entity.UserInteractionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserInteractionLogRepository extends JpaRepository<UserInteractionLog, String> {

    Page<UserInteractionLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    @Query("SELECT u.eventType, COUNT(u) FROM UserInteractionLog u " +
           "WHERE u.timestamp >= :from AND u.timestamp <= :to " +
           "GROUP BY u.eventType ORDER BY COUNT(u) DESC")
    List<Object[]> countByEventTypeBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT u.module, COUNT(u) FROM UserInteractionLog u " +
           "WHERE u.timestamp >= :from AND u.timestamp <= :to " +
           "GROUP BY u.module ORDER BY COUNT(u) DESC")
    List<Object[]> countByModuleBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT FUNCTION('DATE', u.timestamp), COUNT(u) FROM UserInteractionLog u " +
           "WHERE u.timestamp >= :from AND u.timestamp <= :to " +
           "GROUP BY FUNCTION('DATE', u.timestamp) ORDER BY FUNCTION('DATE', u.timestamp)")
    List<Object[]> countByDayBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(DISTINCT u.userId) FROM UserInteractionLog u " +
           "WHERE u.timestamp >= :from AND u.timestamp <= :to")
    long countDistinctUsersBetween(@Param("from") Instant from, @Param("to") Instant to);

    long countByTimestampBetween(Instant from, Instant to);

    Page<UserInteractionLog> findByTimestampBetweenOrderByTimestampDesc(
            Instant from, Instant to, Pageable pageable);
}
