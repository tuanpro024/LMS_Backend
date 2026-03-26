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

    @Query("SELECT s FROM StudySet s JOIN s.folders f WHERE f.id = :folderId AND s.deleted = false")
    List<StudySet> findByFolderId(@Param("folderId") String folderId);

    @Query("SELECT s FROM StudySet s WHERE (s.title LIKE CONCAT('%', :keyword, '%') OR s.description LIKE CONCAT('%', :keyword, '%')) AND s.deleted = false")
    List<StudySet> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM StudySet s WHERE LOWER(s.title) = LOWER(:title) AND s.userId = :userId AND s.deleted = false")
    List<StudySet> findByTitleAndUserIdIgnoreCase(@Param("title") String title, @Param("userId") String userId);

    @Query("SELECT DISTINCT s FROM StudySet s " +
           "JOIN s.folders f " +
           "LEFT JOIN f.packageEntity p1 " +
           "LEFT JOIN f.subject sub1 " +
           "LEFT JOIN sub1.packageEntity p2 " +
           "LEFT JOIN f.slot slot " +
           "LEFT JOIN slot.subject sub2 " +
           "LEFT JOIN sub2.packageEntity p3 " +
           "WHERE ( (p1.type.name = :typeName) " +
           "OR (p2.type.name = :typeName) " +
           "OR (p3.type.name = :typeName) ) " +
           "AND s.deleted = false")
    List<StudySet> findByPackageTypeName(@Param("typeName") com.lms.content.common.entity.TypeName typeName);

    /**
     * Strict filtering:
     * - Study set must have at least one folder that belongs to a package of :typeName
     * - Study set must not have any folder that belongs to packages of a different type
     */
    @Query("SELECT DISTINCT s FROM StudySet s " +
           "WHERE s.deleted = false " +
           "AND EXISTS ( " +
           "  SELECT f FROM s.folders f " +
           "  LEFT JOIN f.packageEntity p1 " +
           "  LEFT JOIN f.subject sub1 " +
           "  LEFT JOIN sub1.packageEntity p2 " +
           "  LEFT JOIN f.slot slot " +
           "  LEFT JOIN slot.subject sub2 " +
           "  LEFT JOIN sub2.packageEntity p3 " +
           "  WHERE (p1.type.name = :typeName OR p2.type.name = :typeName OR p3.type.name = :typeName) " +
           ") " +
           "AND NOT EXISTS ( " +
           "  SELECT f FROM s.folders f " +
           "  LEFT JOIN f.packageEntity p1 " +
           "  LEFT JOIN f.subject sub1 " +
           "  LEFT JOIN sub1.packageEntity p2 " +
           "  LEFT JOIN f.slot slot " +
           "  LEFT JOIN slot.subject sub2 " +
           "  LEFT JOIN sub2.packageEntity p3 " +
           "  WHERE ( (p1.type.name IS NOT NULL AND p1.type.name <> :typeName) " +
           "       OR (p2.type.name IS NOT NULL AND p2.type.name <> :typeName) " +
           "       OR (p3.type.name IS NOT NULL AND p3.type.name <> :typeName) ) " +
           ")")
    List<StudySet> findByPackageTypeNameStrict(@Param("typeName") com.lms.content.common.entity.TypeName typeName);
}
