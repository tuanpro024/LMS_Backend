package com.lms.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lms.notification.entity.Notification;

import java.time.Instant;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    Page<Notification> findByUserId(String userId, Pageable pageable);
    Optional<Notification> findByIdAndUserId(String id, String userId);
    Optional<Notification> findByDedupeKey(String dedupeKey);
    long countByUserIdAndIsReadFalse(String userId);

    @Modifying
    @Query("update Notification n set n.isSeen = true, n.seenAt = :seenAt where n.userId = :userId and n.isSeen = false")
    int markAllSeen(@Param("userId") String userId, @Param("seenAt") Instant seenAt);

    @Modifying
    @Query("update Notification n set n.isRead = true, n.readAt = :readAt, n.isSeen = true, n.seenAt = :readAt where n.userId = :userId and n.isRead = false")
    int markAllRead(@Param("userId") String userId, @Param("readAt") Instant readAt);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "AND (:readStatus IS NULL OR " +
           "     (:readStatus = 'READ' AND n.isRead = true) OR " +
           "     (:readStatus = 'UNREAD' AND n.isRead = false)) " +
           "AND (cast(:fromDate as timestamp) IS NULL OR n.createdAt >= :fromDate) " +
           "AND (cast(:toDate as timestamp) IS NULL OR n.createdAt <= :toDate)")
    Page<Notification> findByFilters(@Param("userId") String userId,
                                    @Param("readStatus") String readStatus,
                                    @Param("fromDate") Instant fromDate,
                                    @Param("toDate") Instant toDate,
                                    Pageable pageable);
}
