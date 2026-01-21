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

    @Query("SELECT f FROM Folder f WHERE f.packageEntity.id = :packageId")
    List<Folder> findByPackageId(@Param("packageId") String packageId);

    @Query("SELECT f FROM Folder f WHERE f.subject.id = :subjectId")
    List<Folder> findBySubjectId(@Param("subjectId") String subjectId);

    @Query("SELECT f FROM Folder f WHERE f.slot.id = :slotId")
    List<Folder> findBySlotId(@Param("slotId") String slotId);
}
