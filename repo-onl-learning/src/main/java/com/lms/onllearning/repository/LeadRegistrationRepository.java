package com.lms.onllearning.repository;

import com.lms.onllearning.entity.LeadRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface LeadRegistrationRepository extends JpaRepository<LeadRegistration, String> {

    /** Kiểm tra duplicate trước khi tạo lead mới */
    Optional<LeadRegistration> findByUserIdAndSyllabusId(String userId, String syllabusId);

    /** Tìm kiếm leads với filter — dùng cho Admin/Staff */
    @Query("""
        SELECT l FROM LeadRegistration l
        WHERE (:syllabusId IS NULL OR l.syllabusId = :syllabusId)
          AND (:from IS NULL OR l.registeredAt >= :from)
          AND (:to IS NULL OR l.registeredAt <= :to)
        ORDER BY l.registeredAt DESC
        """)
    Page<LeadRegistration> findWithFilters(
            @Param("syllabusId") String syllabusId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    /** Streaming query cho Excel export — tránh load toàn bộ vào memory */
    @Query("""
        SELECT l FROM LeadRegistration l
        WHERE (:syllabusId IS NULL OR l.syllabusId = :syllabusId)
          AND (:from IS NULL OR l.registeredAt >= :from)
          AND (:to IS NULL OR l.registeredAt <= :to)
        ORDER BY l.registeredAt ASC
        """)
    Stream<LeadRegistration> streamForExport(
            @Param("syllabusId") String syllabusId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
