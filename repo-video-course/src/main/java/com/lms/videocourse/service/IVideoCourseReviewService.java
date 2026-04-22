package com.lms.videocourse.service;

import com.lms.videocourse.dto.request.UpsertVideoCourseReviewRequest;
import com.lms.videocourse.dto.response.VideoCourseReviewResponse;
import com.lms.videocourse.dto.response.VideoCourseReviewSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IVideoCourseReviewService {

    /**
     * Create or update a review for a package.
     * Validates that the user has access to the package before allowing the review.
     */
    VideoCourseReviewResponse upsertReview(String userId, String displayName, String avatarUrl,
                                            UpsertVideoCourseReviewRequest request);

    /**
     * Delete the current user's review for a package.
     */
    void deleteMyReview(String packageId, String userId);

    /**
     * Get the current user's review for a specific package, or null if none exists.
     */
    VideoCourseReviewResponse getMyReview(String packageId, String userId);

    /**
     * Get paginated list of reviews for a package.
     */
    Page<VideoCourseReviewResponse> getReviewsByPackageId(String packageId, Pageable pageable);

    /**
     * Get review summary (average rating + count) for a single package.
     */
    VideoCourseReviewSummaryResponse getReviewSummary(String packageId);

    /**
     * Batch get review summaries for multiple packages.
     * Key = packageId, Value = summary.
     * Avoids N+1 queries on list pages.
     */
    Map<String, VideoCourseReviewSummaryResponse> getReviewSummaries(List<String> packageIds);
}
