package com.lms.learningpath.repository;

import com.lms.learningpath.entity.UserSetProgress;
import com.lms.learningpath.entity.enums.SetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSetProgressRepository extends JpaRepository<UserSetProgress, String> {

    Optional<UserSetProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<UserSetProgress> findByUserId(String userId);

    List<UserSetProgress> findByUserIdAndStatus(String userId, SetStatus status);

    @Query("SELECT COUNT(p) FROM UserSetProgress p " +
            "WHERE p.userId = :userId " +
            "AND p.status = :status")
    long countByUserIdAndStatus(
            @Param("userId") String userId,
            @Param("status") SetStatus status
    );
}