package com.lms.videocourse.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.common.security.AuthPrincipal;
import com.lms.videocourse.dto.response.VideoCourseWishlistItemResponse;
import com.lms.videocourse.dto.response.VideoCourseWishlistToggleResponse;
import com.lms.videocourse.service.IVideoCourseWishlistService;
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
@RequestMapping("/wishlists/video-courses")
@RequiredArgsConstructor
public class VideoCourseWishlistController {

    private final IVideoCourseWishlistService wishlistService;

    @PostMapping("/{packageId}/toggle")
    public ResponseEntity<ApiResponse<VideoCourseWishlistToggleResponse>> toggleWishlist(
            @PathVariable String packageId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.toggle(userId, packageId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<VideoCourseWishlistItemResponse>>> getMyWishlist(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                wishlistService.getMyWishlist(userId, keyword, category, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/me/ids")
    public ResponseEntity<ApiResponse<List<String>>> getMyWishlistIds(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getMyWishlistPackageIds(userId)));
    }

    private String getUserId(Authentication authentication) {
        return ((AuthPrincipal) authentication.getPrincipal()).userId();
    }
}
