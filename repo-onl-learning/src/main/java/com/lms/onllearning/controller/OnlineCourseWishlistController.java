package com.lms.onllearning.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.onllearning.dto.response.OnlineCourseWishlistItemResponse;
import com.lms.onllearning.dto.response.OnlineCourseWishlistToggleResponse;
import com.lms.onllearning.service.IOnlineCourseWishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/courses/wishlist")
@RequiredArgsConstructor
public class OnlineCourseWishlistController {

    private final IOnlineCourseWishlistService wishlistService;

    @PostMapping("/{courseId}/toggle")
    public ResponseEntity<ApiResponse<OnlineCourseWishlistToggleResponse>> toggleWishlist(
            @PathVariable String courseId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.toggle(userId, courseId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<OnlineCourseWishlistItemResponse>>> getMyWishlist(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                wishlistService.getMyWishlist(userId, keyword, courseType, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/me/ids")
    public ResponseEntity<ApiResponse<List<String>>> getMyWishlistIds(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getMyWishlistCourseIds(userId)));
    }

    private String getUserId(Authentication authentication) {
        return ((AuthPrincipal) authentication.getPrincipal()).userId();
    }
}
