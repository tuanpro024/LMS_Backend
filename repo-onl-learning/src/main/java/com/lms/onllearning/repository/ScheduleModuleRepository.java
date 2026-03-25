package com.lms.onllearning.repository;

import com.lms.onllearning.entity.ScheduleModule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleModuleRepository extends JpaRepository<ScheduleModule, String> {

    /** Lấy danh sách module active, sắp xếp theo thứ tự (cho học sinh xem). */
    List<ScheduleModule> findByScheduleIdAndIsActiveTrueAndDeletedFalseOrderByModuleOrderAsc(String scheduleId);

    /**
     * Kiểm tra tồn tại order TRƯỚC KHI thêm (first-check).
     * Unique constraint ở DB là safety net cuối cùng.
     */
    boolean existsByScheduleIdAndModuleOrderAndDeletedFalse(String scheduleId, Integer moduleOrder);

    Optional<ScheduleModule> findByScheduleIdAndModuleOrderAndDeletedTrue(String scheduleId, Integer moduleOrder);

    /**
     * Pessimistic write lock – dùng trong reorderModules để tránh 2 request
     * cùng lúc cập nhật thứ tự không nhất quán.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM ScheduleModule m WHERE m.scheduleId = :scheduleId AND m.deleted = false")
    List<ScheduleModule> findByScheduleIdForUpdate(@Param("scheduleId") String scheduleId);
}
