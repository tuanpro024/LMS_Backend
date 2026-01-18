package com.lms.writing.repository;

import com.lms.writing.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {
    List<Folder> findByUserId(String userId);

    List<Folder> findByIsPrivateFalse();

    @Query("SELECT f FROM Folder f WHERE f.userId = :userId OR f.isPrivate = false")
    List<Folder> findByUserIdOrIsPrivateFalse(@Param("userId") String userId);

    List<Folder> findByPackageEntityId(String packageId);

    List<Folder> findBySubjectId(String subjectId);

    List<Folder> findBySlotId(String slotId);
}
