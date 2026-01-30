package com.lms.learningpath.repository;

import com.lms.learningpath.entity.UserSectionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSectionProgress entity.
 * Tracks user progress within learning sections.
 */
@Repository
public interface UserSectionProgressRepository extends JpaRepository<UserSectionProgress, String> {

    /**
     * Find progress for a specific user and folder (section)
     */
    Optional<UserSectionProgress> findByUserIdAndFolderId(String userId, String folderId);

    /**
     * Find all progress records for a user across all folders
     */
    List<UserSectionProgress> findByUserId(String userId);

    /**
     * Find all progress records for a specific folder (across all users)
     */
    List<UserSectionProgress> findByFolderId(String folderId);

    /**
     * Find completed sections for a user
     */
    List<UserSectionProgress> findByUserIdAndIsCompletedTrue(String userId);

    /**
     * Delete progress for a specific folder
     */
    void deleteByFolderId(String folderId);

    /**
     * Check if user has completed a section
     */
    boolean existsByUserIdAndFolderIdAndIsCompletedTrue(String userId, String folderId);
}
