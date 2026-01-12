package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Package;
import com.lms.flashcard.entity.PackageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {

    List<Package> findBySubjectCode(String subjectCode);

    List<Package> findByType(PackageType type);

    Optional<Package> findBySubjectCodeAndSlot(String subjectCode, String slot);

    List<Package> findByUserId(String userId);

    List<Package> findBySubjectCodeAndUserId(String subjectCode, String userId);

    List<Package> findByTypeAndUserId(PackageType type, String userId);
}
