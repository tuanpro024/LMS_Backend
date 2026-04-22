package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoCourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoCourseReviewRepository extends JpaRepository<VideoCourseReview, String> {

    Optional<VideoCourseReview> findByPackageIdAndUserIdAndDeletedFalse(String packageId, String userId);

    Page<VideoCourseReview> findByPackageIdAndDeletedFalseAndStatus(String packageId, String status, Pageable pageable);

    long countByPackageIdAndDeletedFalseAndStatus(String packageId, String status);

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0) FROM VideoCourseReview r
            WHERE r.packageId = :packageId AND r.deleted = false AND r.status = 'ACTIVE'
            """)
    double getAverageRatingByPackageId(@Param("packageId") String packageId);

    /**
     * Batch query to get average rating and review count for multiple packages.
     * Returns Object[] where [0]=packageId, [1]=avgRating, [2]=reviewCount.
     */
    @Query("""
            SELECT r.packageId, COALESCE(AVG(r.rating), 0), COUNT(r)
            FROM VideoCourseReview r
            WHERE r.packageId IN :packageIds AND r.deleted = false AND r.status = 'ACTIVE'
            GROUP BY r.packageId
            """)
    List<Object[]> getReviewSummariesByPackageIds(@Param("packageIds") List<String> packageIds);
}
