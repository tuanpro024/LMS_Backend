package com.lms.videocourse.service;

import com.lms.videocourse.dto.response.VideoCourseWishlistItemResponse;
import com.lms.videocourse.dto.response.VideoCourseWishlistToggleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IVideoCourseWishlistService {
    VideoCourseWishlistToggleResponse toggle(String userId, String packageId);

    Page<VideoCourseWishlistItemResponse> getMyWishlist(
            String userId,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable);

    List<String> getMyWishlistPackageIds(String userId);
}
