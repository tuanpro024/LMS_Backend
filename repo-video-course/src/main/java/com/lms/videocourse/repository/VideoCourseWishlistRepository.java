package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoCourseWishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VideoCourseWishlistRepository extends JpaRepository<VideoCourseWishlist, String> {

    Optional<VideoCourseWishlist> findByUserIdAndPackageIdAndDeletedFalse(String userId, String packageId);

    @Query("""
            SELECT w FROM VideoCourseWishlist w
            WHERE w.userId = :userId
              AND w.deleted = false
              AND (:keyword IS NULL OR TRIM(:keyword) = ''
                OR LOWER(w.packageName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(w.packageDescription, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR TRIM(:category) = '' OR w.packageCategory = :category)
              AND (:minPrice IS NULL OR w.packagePrice >= :minPrice)
              AND (:maxPrice IS NULL OR w.packagePrice <= :maxPrice)
            """)
    Page<VideoCourseWishlist> findMyWishlist(
            @Param("userId") String userId,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
            SELECT w.packageId FROM VideoCourseWishlist w
            WHERE w.userId = :userId AND w.deleted = false
            ORDER BY w.createdAt DESC
            """)
    List<String> findPackageIdsByUserId(@Param("userId") String userId);
}
