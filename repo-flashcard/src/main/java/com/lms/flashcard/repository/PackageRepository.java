package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Package;
import com.lms.flashcard.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {

    List<Package> findByType(Type type);

    List<Package> findByUserId(String userId);

    List<Package> findByTypeAndUserId(Type type, String userId);
}
