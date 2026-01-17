package com.lms.writing.repository;

import com.lms.writing.entity.Type;
import com.lms.writing.entity.TypeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {
    Optional<Type> findByName(TypeName name);
}
