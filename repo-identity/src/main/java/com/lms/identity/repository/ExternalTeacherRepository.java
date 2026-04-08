package com.lms.identity.repository;

import com.lms.identity.entity.ExternalTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalTeacherRepository extends JpaRepository<ExternalTeacher, String> {
    Optional<ExternalTeacher> findByEmail(String email);
    Optional<ExternalTeacher> findByCmsUserId(String cmsUserId);
    boolean existsByEmail(String email);
}
