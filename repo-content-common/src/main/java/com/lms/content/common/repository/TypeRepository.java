package com.lms.content.common.repository;

import com.lms.content.common.entity.Type;
import com.lms.content.common.entity.TypeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeRepository extends JpaRepository<Type, String> {
    Optional<Type> findByName(TypeName name);
}
