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

    @Query("SELECT f FROM Folder f " +
            "LEFT JOIN f.subject s " +
            "LEFT JOIN s.packageEntity sp " +
            "LEFT JOIN f.slot sl " +
            "LEFT JOIN sl.subject sls " +
            "LEFT JOIN sls.packageEntity slsp " +
            "WHERE f.packageEntity.id = :packageId " +
            "OR sp.id = :packageId " +
            "OR slsp.id = :packageId")
    List<Folder> findByPackageId(@Param("packageId") String packageId);

    @Query("SELECT f FROM Folder f WHERE f.subject.id = :subjectId")
    List<Folder> findBySubjectId(@Param("subjectId") String subjectId);

    @Query("SELECT f FROM Folder f WHERE f.slot.id = :slotId")
    List<Folder> findBySlotId(@Param("slotId") String slotId);

    // Find folders by package entity id (Spring Data JPA naming convention)
    List<Folder> findByPackageEntityId(String packageId);

    // Find folders by package entity id ordered by creation date
    List<Folder> findByPackageEntityIdOrderByCreatedAtAsc(String packageId);
}
