package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.videocourse.dto.request.UpsertVideoCourseReviewRequest;
import com.lms.videocourse.dto.response.VideoCourseReviewResponse;
import com.lms.videocourse.dto.response.VideoCourseReviewSummaryResponse;
import com.lms.videocourse.service.IVideoCourseReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class VideoCourseReviewController {

    private final IVideoCourseReviewService reviewService;

    /**
     * Upsert review — create if not exists, update if exists.
     * Requires the user to have access (enrolled/purchased) to the package.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VideoCourseReviewResponse>> upsertReview(
            @RequestBody @Valid UpsertVideoCourseReviewRequest request,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        // AuthPrincipal only has userId + email; use email as fallback displayName
        String displayName = principal.email() != null ? principal.email().split("@")[0] : principal.userId();
        VideoCourseReviewResponse response = reviewService.upsertReview(
                principal.userId(),
                displayName,
                null,
                request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Delete the current user's review for a package.
     */
    @DeleteMapping("/package/{packageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyReview(
            @PathVariable String packageId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        reviewService.deleteMyReview(packageId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Get the current user's review for a specific package.
     * Returns null body if no review exists.
     */
    @GetMapping("/package/{packageId}/me")
    public ResponseEntity<ApiResponse<VideoCourseReviewResponse>> getMyReview(
            @PathVariable String packageId,
            Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        VideoCourseReviewResponse review = reviewService.getMyReview(packageId, principal.userId());
        return ResponseEntity.ok(ApiResponse.ok(review));
    }

    /**
     * Get paginated list of reviews for a package.
     * Public endpoint — no authentication required.
     */
    @GetMapping("/package/{packageId}")
    public ResponseEntity<ApiResponse<Page<VideoCourseReviewResponse>>> getReviewsByPackageId(
            @PathVariable String packageId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviewsByPackageId(packageId, pageable)));
    }

    /**
     * Get review summary (average rating + count) for a package.
     * Public endpoint — no authentication required.
     */
    @GetMapping("/package/{packageId}/summary")
    public ResponseEntity<ApiResponse<VideoCourseReviewSummaryResponse>> getReviewSummary(
            @PathVariable String packageId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviewSummary(packageId)));
    }
}
