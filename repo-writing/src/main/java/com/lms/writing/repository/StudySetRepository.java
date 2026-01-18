package com.lms.writing.repository;

import com.lms.writing.entity.StudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudySetRepository extends JpaRepository<StudySet, String> {
    List<StudySet> findByUserId(String userId);

    List<StudySet> findByIsPrivateFalse();

    @Query("SELECT s FROM StudySet s WHERE s.userId = :userId OR s.isPrivate = false")
    List<StudySet> findByUserIdOrIsPrivateFalse(@Param("userId") String userId);

    @Query("SELECT s FROM StudySet s WHERE (s.isPrivate = false OR s.userId = :userId) AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<StudySet> searchByTitleOrDescription(@Param("query") String query, @Param("userId") String userId);
}
