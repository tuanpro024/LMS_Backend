package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.OnlineCourseWishlistItemResponse;
import com.lms.onllearning.dto.response.OnlineCourseWishlistToggleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IOnlineCourseWishlistService {
    OnlineCourseWishlistToggleResponse toggle(String userId, String courseId);

    Page<OnlineCourseWishlistItemResponse> getMyWishlist(
            String userId,
            String keyword,
            String courseType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable);

    List<String> getMyWishlistCourseIds(String userId);
}
