package com.lms.learningpath.repository;

import com.lms.learningpath.entity.StudySetProgress;
import com.lms.learningpath.entity.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudySetProgressRepository extends JpaRepository<StudySetProgress, String> {

    Optional<StudySetProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    List<StudySetProgress> findByUserIdAndFolderId(String userId, String folderId);

    List<StudySetProgress> findByUserIdAndPackageId(String userId, String packageId);

    List<StudySetProgress> findByUserId(String userId);

    @Query("SELECT sp FROM StudySetProgress sp WHERE sp.userId = :userId " +
            "AND sp.folderId = :folderId AND sp.status = 'COMPLETED' " +
            "ORDER BY sp.completedAt DESC")
    List<StudySetProgress> findCompletedSetsByUserAndFolder(@Param("userId") String userId,
            @Param("folderId") String folderId);

    void deleteByStudySetId(String studySetId);
}
