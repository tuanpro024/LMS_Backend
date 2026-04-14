package com.lms.onllearning.repository;

import com.lms.onllearning.entity.OnlineCourseWishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OnlineCourseWishlistRepository extends JpaRepository<OnlineCourseWishlist, String> {

    Optional<OnlineCourseWishlist> findByUserIdAndCourseIdAndDeletedFalse(String userId, String courseId);

    @Query("""
            SELECT w FROM OnlineCourseWishlist w
            JOIN OnlineCourse c ON c.id = w.courseId
            WHERE w.userId = :userId
              AND w.deleted = false
              AND c.deleted = false
              AND (:keyword IS NULL OR TRIM(:keyword) = ''
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:courseType IS NULL OR TRIM(:courseType) = '' OR c.courseType = :courseType)
              AND (:minPrice IS NULL OR c.price >= :minPrice)
              AND (:maxPrice IS NULL OR c.price <= :maxPrice)
            """)
    Page<OnlineCourseWishlist> findMyWishlist(
            @Param("userId") String userId,
            @Param("keyword") String keyword,
            @Param("courseType") String courseType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
            SELECT w.courseId FROM OnlineCourseWishlist w
            WHERE w.userId = :userId AND w.deleted = false
            ORDER BY w.createdAt DESC
            """)
    List<String> findCourseIdsByUserId(@Param("userId") String userId);
}
