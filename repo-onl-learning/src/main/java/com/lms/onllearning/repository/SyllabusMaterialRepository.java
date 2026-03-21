package com.lms.onllearning.repository;

import com.lms.onllearning.entity.SyllabusMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusMaterialRepository extends JpaRepository<SyllabusMaterial, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM SyllabusMaterial m WHERE m.syllabus.id = :syllabusId")
    void deleteBySyllabusId(String syllabusId);


    @Query("SELECT m FROM SyllabusMaterial m WHERE m.syllabus.id = :syllabusId")
    List<SyllabusMaterial> findBySyllabusId(String syllabusId);

}
