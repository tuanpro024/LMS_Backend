package com.lms.writing.repository;

import com.lms.writing.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, String> {
    List<Slot> findBySubjectId(String subjectId);

    List<Slot> findByUserId(String userId);
}
