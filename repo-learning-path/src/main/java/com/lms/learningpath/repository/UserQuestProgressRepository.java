package com.lms.learningpath.repository;

import com.lms.learningpath.entity.UserQuestProgress;
import com.lms.learningpath.entity.enums.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuestProgressRepository extends JpaRepository<UserQuestProgress, String> {

    Optional<UserQuestProgress> findByUserIdAndQuestIdAndPeriod(
            String userId,
            String questId,
            String period
    );

    List<UserQuestProgress> findByUserIdAndStatus(String userId, QuestStatus status);

    List<UserQuestProgress> findByUserIdAndStatusIn(String userId, List<QuestStatus> statuses);

    @Query("SELECT p FROM UserQuestProgress p " +
            "WHERE p.userId = :userId " +
            "AND p.status IN :statuses " +
            "AND (p.expiresAt IS NULL OR p.expiresAt >= :now)")
    List<UserQuestProgress> findActiveQuestsByUser(
            @Param("userId") String userId,
            @Param("statuses") List<QuestStatus> statuses,
            @Param("now") Instant now
    );

    @Query("SELECT COUNT(p) FROM UserQuestProgress p " +
            "WHERE p.userId = :userId " +
            "AND p.status = 'COMPLETED' " +
            "AND p.completedAt BETWEEN :start AND :end")
    long countCompletedQuestsByUserAndPeriod(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}