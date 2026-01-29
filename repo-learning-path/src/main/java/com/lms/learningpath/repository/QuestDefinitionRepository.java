package com.lms.learningpath.repository;

import com.lms.learningpath.entity.QuestDefinition;
import com.lms.learningpath.entity.enums.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface QuestDefinitionRepository extends JpaRepository<QuestDefinition, String> {

    List<QuestDefinition> findByIsActiveTrue();

    List<QuestDefinition> findByTypeAndIsActiveTrue(QuestType type);

    @Query("SELECT q FROM QuestDefinition q " +
            "WHERE q.isActive = true " +
            "AND (q.startDate IS NULL OR q.startDate <= :now) " +
            "AND (q.endDate IS NULL OR q.endDate >= :now)")
    List<QuestDefinition> findActiveQuestsAtTime(@Param("now") Instant now);

    @Query("SELECT q FROM QuestDefinition q " +
            "WHERE q.isActive = true " +
            "AND q.type = :type " +
            "AND (q.startDate IS NULL OR q.startDate <= :now) " +
            "AND (q.endDate IS NULL OR q.endDate >= :now)")
    List<QuestDefinition> findActiveQuestsByTypeAtTime(
            @Param("type") QuestType type,
            @Param("now") Instant now
    );
}