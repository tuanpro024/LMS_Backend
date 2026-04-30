package com.lms.onllearning.repository;

import com.lms.onllearning.entity.LeadRegistration;
import com.lms.onllearning.entity.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface LeadRegistrationRepository extends JpaRepository<LeadRegistration, String> {

  /** Kiểm tra duplicate trước khi tạo lead mới */
  Optional<LeadRegistration> findByUserIdAndCourseCode(String userId, String courseCode);

  /** Tìm kiếm leads với filter — dùng cho Admin/Staff */
  @Query("""
          SELECT l FROM LeadRegistration l
      WHERE (:courseCode IS NULL OR l.courseCode = :courseCode)
            AND (:from IS NULL OR l.registeredAt >= :from)
            AND (:to IS NULL OR l.registeredAt <= :to)
          ORDER BY l.registeredAt DESC
          """)
  Page<LeadRegistration> findWithFilters(
      @Param("courseCode") String courseCode,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);

  /** Streaming query cho Excel export — tránh load toàn bộ vào memory */
  @Query("""
      SELECT l FROM LeadRegistration l
      WHERE (:courseCode IS NULL OR l.courseCode = :courseCode)
        AND (:from IS NULL OR l.registeredAt >= :from)
        AND (:to IS NULL OR l.registeredAt <= :to)
      ORDER BY l.registeredAt ASC
      """)
  Stream<LeadRegistration> streamForExport(
      @Param("courseCode") String courseCode,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  /** Tìm tất cả leads đang PENDING — dùng cho reconciler */
  List<LeadRegistration> findByStatus(RegistrationStatus status);

  /** Bulk cancel: PENDING quá hạn → CANCELED */
  @Modifying
  @Query("""
      UPDATE LeadRegistration l
      SET l.status = com.lms.onllearning.entity.enums.RegistrationStatus.CANCELED
      WHERE l.status = com.lms.onllearning.entity.enums.RegistrationStatus.PENDING
        AND l.registeredAt < :cutoff
      """)
  int cancelExpiredPendingLeads(@Param("cutoff") LocalDateTime cutoff);
}
