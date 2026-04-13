package com.lms.videocourse.service.impl;

import com.lms.content.common.delegate.api.PackageApiDelegate;
import com.lms.content.common.dto.response.PackageResponse;
import com.lms.content.common.entity.TypeName;
import com.lms.videocourse.dto.response.VideoCourseWishlistItemResponse;
import com.lms.videocourse.dto.response.VideoCourseWishlistToggleResponse;
import com.lms.videocourse.entity.VideoCourseWishlist;
import com.lms.videocourse.repository.VideoCourseWishlistRepository;
import com.lms.videocourse.service.IVideoCourseWishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoCourseWishlistServiceImpl implements IVideoCourseWishlistService {

    private final VideoCourseWishlistRepository wishlistRepository;
    private final PackageApiDelegate packageApiDelegate;

    @Override
    @Transactional
    public VideoCourseWishlistToggleResponse toggle(String userId, String packageId) {
        PackageResponse pkg = packageApiDelegate.getPackageById(packageId);
        if (pkg == null || pkg.getId() == null) {
            throw new IllegalArgumentException("Không tìm thấy gói khóa học video: " + packageId);
        }

        if (pkg.getType() != null && pkg.getType() != TypeName.VIDEO_COURSE) {
            throw new IllegalArgumentException("Package không thuộc loại VIDEO_COURSE: " + packageId);
        }

        return wishlistRepository.findByUserIdAndPackageIdAndDeletedFalse(userId, packageId)
                .map(existing -> {
                    wishlistRepository.delete(existing);
                    return VideoCourseWishlistToggleResponse.builder()
                            .packageId(packageId)
                            .inWishlist(false)
                            .addedAt(null)
                            .build();
                })
                .orElseGet(() -> {
                    VideoCourseWishlist saved = wishlistRepository.save(VideoCourseWishlist.builder()
                            .userId(userId)
                            .packageId(pkg.getId())
                            .packageName(pkg.getName())
                            .packageDescription(pkg.getDescription())
                            .packageType(pkg.getType() == null ? null : pkg.getType().name())
                            .packageCategory(pkg.getCategory() == null ? null : pkg.getCategory().name())
                            .packageThumbnail(pkg.getThumbnail())
                            .packagePrice(pkg.getPrice())
                            .build());

                    return VideoCourseWishlistToggleResponse.builder()
                            .packageId(packageId)
                            .inWishlist(true)
                            .addedAt(saved.getCreatedAt())
                            .build();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoCourseWishlistItemResponse> getMyWishlist(
            String userId,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        return wishlistRepository.findMyWishlist(userId, keyword, category, minPrice, maxPrice, pageable)
                .map(w -> VideoCourseWishlistItemResponse.builder()
                        .id(w.getId())
                        .packageId(w.getPackageId())
                        .name(w.getPackageName())
                        .description(w.getPackageDescription())
                        .type(w.getPackageType())
                        .category(w.getPackageCategory())
                        .thumbnail(w.getPackageThumbnail())
                        .price(w.getPackagePrice())
                        .addedAt(w.getCreatedAt())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMyWishlistPackageIds(String userId) {
        return wishlistRepository.findPackageIdsByUserId(userId);
    }
}
