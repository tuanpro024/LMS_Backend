package com.lms.writing.repository;

import com.lms.writing.entity.Package;
import com.lms.writing.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {
    List<Package> findByUserId(String userId);

    List<Package> findByType(Type type);

    List<Package> findByTypeAndUserId(Type type, String userId);
}
