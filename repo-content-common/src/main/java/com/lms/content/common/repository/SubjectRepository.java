package com.lms.content.common.repository;

import com.lms.content.common.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {

    List<Subject> findByPackageEntityId(String packageId);

    Optional<Subject> findByIdAndUserId(String id, String userId);
}
