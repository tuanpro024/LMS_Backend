package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {

    List<Subject> findByPackageEntityId(String packageId);

    List<Subject> findByPackageEntityIdAndUserId(String packageId, String userId);

    List<Subject> findByUserId(String userId);

    Optional<Subject> findByIdAndUserId(String id, String userId);
}
