package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, String> {

    List<Slot> findBySubjectId(String subjectId);

    List<Slot> findBySubjectIdAndUserId(String subjectId, String userId);

    List<Slot> findByUserId(String userId);

    Optional<Slot> findByIdAndUserId(String id, String userId);
}
