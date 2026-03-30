package com.lms.content.common.repository;

import com.lms.content.common.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {

    List<Folder> findByUserId(String userId);

    @Query("""
            SELECT DISTINCT f
            FROM Folder f
            WHERE f.packageEntity.id = :packageId
            """)
    List<Folder> findByPackageId(@Param("packageId") String packageId);

    // Find folders by package entity id (Spring Data JPA naming convention)
    List<Folder> findByPackageEntityId(String packageId);

    // Find folders by package entity id ordered by creation date
    List<Folder> findByPackageEntityIdOrderByCreatedAtAsc(String packageId);

    @Query("SELECT f FROM Folder f JOIN f.studySets s WHERE s.id = :studySetId")
    List<Folder> findByStudySetId(@Param("studySetId") String studySetId);
}
