package com.lms.onllearning.service.impl;

import com.lms.onllearning.dto.response.OnlineCourseResponse;
import com.lms.onllearning.dto.response.OnlineCourseWishlistItemResponse;
import com.lms.onllearning.dto.response.OnlineCourseWishlistToggleResponse;
import com.lms.onllearning.entity.OnlineCourse;
import com.lms.onllearning.entity.OnlineCourseWishlist;
import com.lms.onllearning.repository.OnlineCourseRepository;
import com.lms.onllearning.repository.OnlineCourseWishlistRepository;
import com.lms.onllearning.service.IOnlineCourseWishlistService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineCourseWishlistServiceImpl implements IOnlineCourseWishlistService {

    private final OnlineCourseWishlistRepository wishlistRepository;
    private final OnlineCourseRepository onlineCourseRepository;

    @Override
    @Transactional
    public OnlineCourseWishlistToggleResponse toggle(String userId, String courseId) {
        OnlineCourse course = onlineCourseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học: " + courseId));

        return wishlistRepository.findByUserIdAndCourseIdAndDeletedFalse(userId, courseId)
                .map(existing -> {
                    wishlistRepository.delete(existing);
                    return new OnlineCourseWishlistToggleResponse(courseId, false, null);
                })
                .orElseGet(() -> {
                    OnlineCourseWishlist created = wishlistRepository.save(
                            OnlineCourseWishlist.builder()
                                    .userId(userId)
                                    .courseId(courseId)
                                    .build());
                    return new OnlineCourseWishlistToggleResponse(courseId, true, created.getCreatedAt());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OnlineCourseWishlistItemResponse> getMyWishlist(
            String userId,
            String keyword,
            String courseType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        Page<OnlineCourseWishlist> page = wishlistRepository.findMyWishlist(
                userId,
                keyword,
                courseType,
                minPrice,
                maxPrice,
                pageable);

        List<OnlineCourseWishlistItemResponse> items = page.getContent()
                .stream()
                .map(wishlist -> {
                    OnlineCourse course = onlineCourseRepository.findByIdAndDeletedFalse(wishlist.getCourseId())
                            .orElse(null);
                    if (course == null) {
                        return null;
                    }
                    return new OnlineCourseWishlistItemResponse(
                            wishlist.getId(),
                            wishlist.getCourseId(),
                            wishlist.getCreatedAt(),
                            toResponse(course));
                })
                .filter(item -> item != null)
                .toList();

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMyWishlistCourseIds(String userId) {
        return wishlistRepository.findCourseIdsByUserId(userId);
    }

    private OnlineCourseResponse toResponse(OnlineCourse e) {
        return new OnlineCourseResponse(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getCourseType(),
                e.getLevel(),
                e.getTotalLessons(),
                e.getThumbnail(),
                e.getDescription(),
                e.getSyllabusId(),
                e.getPrice(),
                e.getRating(),
                e.isCmsSynced(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
