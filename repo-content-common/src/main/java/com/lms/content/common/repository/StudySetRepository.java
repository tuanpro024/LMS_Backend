package com.lms.content.common.repository;

import com.lms.content.common.entity.StudySet;
import com.lms.content.common.entity.enums.PublishStatus;
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
                     "LEFT JOIN f.packageEntity p " +
                     "WHERE p.type.name = :typeName " +
                     "AND s.deleted = false")
       List<StudySet> findByPackageTypeName(@Param("typeName") com.lms.content.common.entity.TypeName typeName);

       /**
        * Strict filtering:
        * - Study set must have at least one folder that belongs to a package of
        * :typeName
        * - Study set must not have any folder that belongs to packages of a different
        * type
        */
       @Query("SELECT DISTINCT s FROM StudySet s " +
                     "WHERE s.deleted = false " +
                     "AND EXISTS ( " +
                     "  SELECT f FROM s.folders f " +
                     "  LEFT JOIN f.packageEntity p " +
                     "  WHERE p.type.name = :typeName " +
                     ") " +
                     "AND NOT EXISTS ( " +
                     "  SELECT f FROM s.folders f " +
                     "  LEFT JOIN f.packageEntity p " +
                     "  WHERE (p.type.name IS NOT NULL AND p.type.name <> :typeName) " +
                     ")")
       List<StudySet> findByPackageTypeNameStrict(@Param("typeName") com.lms.content.common.entity.TypeName typeName);

       // ── Public: chỉ study set trong package PUBLISHED ─────────────────────────

       /**
        * Study sets chỉ trong package đang PUBLISHED (dùng cho public GET).
        */
       @Query("SELECT DISTINCT s FROM StudySet s " +
                     "JOIN s.folders f " +
                     "LEFT JOIN f.packageEntity p " +
                     "WHERE p.type.name = :typeName " +
                     "AND p.publishStatus = :status " +
                     "AND s.deleted = false")
       List<StudySet> findByPackageTypeNameAndPublishStatus(
                     @Param("typeName") com.lms.content.common.entity.TypeName typeName,
                     @Param("status") PublishStatus status);

       /**
        * Study sets trong package PUBLISHED hoặc DRAFT do userId tạo (ticket-holder).
        */
       @Query("SELECT DISTINCT s FROM StudySet s " +
                     "JOIN s.folders f " +
                     "LEFT JOIN f.packageEntity p " +
                     "WHERE p.type.name = :typeName " +
                     "AND (p.publishStatus = 'PUBLISHED' OR (p.publishStatus = 'DRAFT' AND p.userId = :userId)) " +
                     "AND s.deleted = false")
       List<StudySet> findByPackageTypeNameForTicketHolder(
                     @Param("typeName") com.lms.content.common.entity.TypeName typeName,
                     @Param("userId") String userId);

       /**
        * Count active folders linked to this study set.
        */
       @Query("SELECT COUNT(f) FROM StudySet s JOIN s.folders f " +
                     "WHERE s.id = :studySetId AND s.deleted = false AND f.deleted = false")
       long countActiveLinkedFolders(@Param("studySetId") String studySetId);

       /**
        * Returns true if this study set belongs to at least one active PUBLISHED package.
        */
       @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StudySet s " +
                     "JOIN s.folders f " +
                     "JOIN f.packageEntity p " +
                     "WHERE s.id = :studySetId " +
                     "AND s.deleted = false " +
                     "AND f.deleted = false " +
                     "AND p.deleted = false " +
                     "AND p.publishStatus = 'PUBLISHED'")
       boolean existsInPublishedPackage(@Param("studySetId") String studySetId);
}
