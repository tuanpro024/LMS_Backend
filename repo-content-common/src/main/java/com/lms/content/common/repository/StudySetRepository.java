package com.lms.content.common.repository;

import com.lms.content.common.entity.StudySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudySetRepository extends JpaRepository<StudySet, String> {

    List<StudySet> findByUserId(String userId);

    @Query("SELECT s FROM StudySet s JOIN s.folders f WHERE f.id = :folderId")
    List<StudySet> findByFolderId(@Param("folderId") String folderId);

    @Query("SELECT s FROM StudySet s WHERE s.title LIKE CONCAT('%', :keyword, '%') OR s.description LIKE CONCAT('%', :keyword, '%')")
    List<StudySet> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM StudySet s WHERE LOWER(s.title) = LOWER(:title) AND s.userId = :userId")
    List<StudySet> findByTitleAndUserIdIgnoreCase(@Param("title") String title, @Param("userId") String userId);
}
