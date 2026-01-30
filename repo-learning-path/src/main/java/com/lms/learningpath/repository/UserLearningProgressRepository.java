package com.lms.learningpath.repository;

import com.lms.learningpath.entity.UserLearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserLearningProgress entity.
 * Tracks user progress through entire learning paths.
 */
@Repository
public interface UserLearningProgressRepository extends JpaRepository<UserLearningProgress, String> {

    /**
     * Find progress for a specific user and package (learning path)
     */
    Optional<UserLearningProgress> findByUserIdAndPackageId(String userId, String packageId);

    /**
     * Find all learning paths a user has started
     */
    List<UserLearningProgress> findByUserId(String userId);

    /**
     * Find all users who have started a specific learning path
     */
    List<UserLearningProgress> findByPackageId(String packageId);

    /**
     * Find completed learning paths for a user
     */
    List<UserLearningProgress> findByUserIdAndCompletedAtIsNotNull(String userId);

    /**
     * Delete progress for a specific package
     */
    void deleteByPackageId(String packageId);

    /**
     * Count users enrolled in a learning path
     */
    long countByPackageId(String packageId);
}
