package com.lms.content.common.repository;

import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.TypeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName")
    List<Package> findByTypeName(TypeName typeName);
}
