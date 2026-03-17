package com.lms.onllearning.repository;

import com.lms.onllearning.entity.SyllabusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusScheduleRepository extends JpaRepository<SyllabusSchedule, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusSchedule s WHERE s.syllabus.id = :syllabusId")
    void deleteBySyllabusId(String syllabusId);

    List<SyllabusSchedule> findBySyllabusIdOrderBySessionNoAsc(String syllabusId);
}
