package com.lms.onllearning.repository;

import com.lms.onllearning.entity.SyllabusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SyllabusScheduleRepository extends JpaRepository<SyllabusSchedule, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusSchedule s WHERE s.syllabus.id = :syllabusId")
    void deleteBySyllabusId(String syllabusId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusSchedule s WHERE s.syllabus.id = :syllabusId AND s.id NOT IN :scheduleIds")
    void deleteBySyllabusIdAndIdNotIn(String syllabusId, List<String> scheduleIds);

    /** Alias dùng bởi SyllabusServiceImpl (không cần thứ tự cụ thể). */
    @Query("SELECT s FROM SyllabusSchedule s WHERE s.syllabus.id = :syllabusId")
    List<SyllabusSchedule> findBySyllabusId(String syllabusId);

    List<SyllabusSchedule> findBySyllabusIdOrderBySessionNoAsc(String syllabusId);

    Optional<SyllabusSchedule> findFirstBySyllabus_IdAndSessionNoGreaterThanOrderBySessionNoAsc(
            String syllabusId,
            Integer sessionNo);
}
