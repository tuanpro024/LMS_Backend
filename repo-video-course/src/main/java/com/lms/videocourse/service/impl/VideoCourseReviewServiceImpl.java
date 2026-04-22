package com.lms.videocourse.service.impl;

import com.lms.common.dto.ApiResponse;
import com.lms.videocourse.client.PaymentClient;
import com.lms.videocourse.client.dto.PaymentAccessCheckResponse;
import com.lms.videocourse.dto.request.UpsertVideoCourseReviewRequest;
import com.lms.videocourse.dto.response.VideoCourseReviewResponse;
import com.lms.videocourse.dto.response.VideoCourseReviewSummaryResponse;
import com.lms.videocourse.entity.VideoCourseReview;
import com.lms.videocourse.repository.VideoCourseReviewRepository;
import com.lms.videocourse.service.IVideoCourseReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCourseReviewServiceImpl implements IVideoCourseReviewService {

    private final VideoCourseReviewRepository reviewRepository;
    private final PaymentClient paymentClient;

    @Override
    @Transactional
    public VideoCourseReviewResponse upsertReview(String userId, String displayName, String avatarUrl,
                                                   UpsertVideoCourseReviewRequest request) {
        // 1. Validate access via payment service
        validateAccess(request.getPackageId());

        // 2. Find existing review or create new
        VideoCourseReview review = reviewRepository
                .findByPackageIdAndUserIdAndDeletedFalse(request.getPackageId(), userId)
                .orElse(null);

        if (review != null) {
            // Update existing
            review.setRating(request.getRating());
            review.setFeedback(trimFeedback(request.getFeedback()));
            review.setDisplayName(displayName);
            if (avatarUrl != null) {
                review.setAvatarUrl(avatarUrl);
            }
        } else {
            // Create new
            review = VideoCourseReview.builder()
                    .userId(userId)
                    .packageId(request.getPackageId())
                    .rating(request.getRating())
                    .feedback(trimFeedback(request.getFeedback()))
                    .displayName(displayName)
                    .avatarUrl(avatarUrl)
                    .status("ACTIVE")
                    .build();
        }

        review = reviewRepository.save(review);
        return toResponse(review);
    }

    @Override
    @Transactional
    public void deleteMyReview(String packageId, String userId) {
        VideoCourseReview review = reviewRepository
                .findByPackageIdAndUserIdAndDeletedFalse(packageId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bạn chưa đánh giá khóa học này"));

        review.setDeleted(true);
        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoCourseReviewResponse getMyReview(String packageId, String userId) {
        return reviewRepository.findByPackageIdAndUserIdAndDeletedFalse(packageId, userId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoCourseReviewResponse> getReviewsByPackageId(String packageId, Pageable pageable) {
        return reviewRepository.findByPackageIdAndDeletedFalseAndStatus(packageId, "ACTIVE", pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoCourseReviewSummaryResponse getReviewSummary(String packageId) {
        long count = reviewRepository.countByPackageIdAndDeletedFalseAndStatus(packageId, "ACTIVE");
        double avg = count > 0 ? reviewRepository.getAverageRatingByPackageId(packageId) : 0.0;
        return VideoCourseReviewSummaryResponse.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0) // Round to 1 decimal
                .reviewCount(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, VideoCourseReviewSummaryResponse> getReviewSummaries(List<String> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = reviewRepository.getReviewSummariesByPackageIds(packageIds);
        Map<String, VideoCourseReviewSummaryResponse> map = new HashMap<>();
        for (Object[] row : results) {
            String pkgId = (String) row[0];
            double avg = ((Number) row[1]).doubleValue();
            long count = ((Number) row[2]).longValue();
            map.put(pkgId, VideoCourseReviewSummaryResponse.builder()
                    .averageRating(Math.round(avg * 10.0) / 10.0)
                    .reviewCount(count)
                    .build());
        }
        return map;
    }

    // ========== Private helpers ==========

    private void validateAccess(String packageId) {
        try {
            ApiResponse<PaymentAccessCheckResponse> response = paymentClient.checkAccess(packageId);
            if (response == null || response.data() == null || !response.data().isHasAccess()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Bạn cần đăng ký khóa học trước khi đánh giá");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking payment access for package {}: {}", packageId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể xác minh quyền truy cập khóa học. Vui lòng thử lại sau.");
        }
    }

    private String trimFeedback(String feedback) {
        return feedback == null ? null : feedback.trim();
    }

    private VideoCourseReviewResponse toResponse(VideoCourseReview review) {
        return VideoCourseReviewResponse.builder()
                .id(review.getId())
                .packageId(review.getPackageId())
                .userId(review.getUserId())
                .displayName(review.getDisplayName())
                .avatarUrl(review.getAvatarUrl())
                .rating(review.getRating())
                .feedback(review.getFeedback())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
