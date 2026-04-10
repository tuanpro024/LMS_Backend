package com.lms.identity.repository;

import com.lms.identity.entity.ExternalTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalTeacherRepository extends JpaRepository<ExternalTeacher, String> {
    Optional<ExternalTeacher> findByEmail(String email);
    Optional<ExternalTeacher> findByCmsUserId(String cmsUserId);
    boolean existsByEmail(String email);

    @Query("SELECT t FROM ExternalTeacher t WHERE t.deleted = false ORDER BY COALESCE(t.rating, 0) DESC, t.fullName ASC")
    List<ExternalTeacher> findAllActiveOrderByRatingDesc();

    @Query("SELECT t FROM ExternalTeacher t WHERE t.id = :id AND t.deleted = false")
    Optional<ExternalTeacher> findActiveById(@Param("id") String id);
}
