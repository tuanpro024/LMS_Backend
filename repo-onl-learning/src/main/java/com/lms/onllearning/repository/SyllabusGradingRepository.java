package com.lms.onllearning.repository;

import com.lms.onllearning.entity.SyllabusGrading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusGradingRepository extends JpaRepository<SyllabusGrading, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusGrading g WHERE g.syllabus.id = :syllabusId")
    void deleteBySyllabusId(String syllabusId);

    @Query("SELECT g FROM SyllabusGrading g WHERE g.syllabus.id = :syllabusId")
    List<SyllabusGrading> findBySyllabusId(String syllabusId);

}
