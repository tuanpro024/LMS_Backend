package com.lms.content.common.repository;

import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName")
    List<Package> findByTypeName(TypeName typeName, Pageable pageable);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName")
    List<Package> findByTypeName(TypeName typeName);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.category = :category")
    List<Package> findByTypeNameAndCategory(TypeName typeName, CategoryType category);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.pricingType = :pricingType")
    List<Package> findByTypeNameAndPricingType(TypeName typeName, String pricingType, Pageable pageable);
}
