package com.lms.onllearning.repository;

import com.lms.onllearning.entity.SyllabusClo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusCloRepository extends JpaRepository<SyllabusClo, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusClo c WHERE c.syllabus.id = :syllabusId")
    void deleteBySyllabusId(String syllabusId);

    List<SyllabusClo> findBySyllabusId(String syllabusId);
}
