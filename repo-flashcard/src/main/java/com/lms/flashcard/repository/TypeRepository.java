package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.flashcard.entity.TypeName;

import java.util.Optional;

public interface TypeRepository extends JpaRepository<Type, String> {
    Optional<Type> findByName(TypeName name);
}
