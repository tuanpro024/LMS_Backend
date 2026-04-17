package com.lms.content.common.repository;

import com.lms.content.common.entity.Package;
import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.entity.enums.PublishStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {

    // ── Admin / Manager: xem tất cả (không filter theo publishStatus) ──────────

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName")
    List<Package> findByTypeName(TypeName typeName, Pageable pageable);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName")
    List<Package> findByTypeName(TypeName typeName);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.category = :category")
    List<Package> findByTypeNameAndCategory(TypeName typeName, CategoryType category);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.pricingType = :pricingType")
    List<Package> findByTypeNameAndPricingType(TypeName typeName, String pricingType, Pageable pageable);

    // ── Public (user thường): chỉ PUBLISHED ────────────────────────────────────

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.publishStatus = :status")
    List<Package> findByTypeNameAndPublishStatus(
            @Param("typeName") TypeName typeName,
            @Param("status") PublishStatus status,
            Pageable pageable);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.publishStatus = :status")
    List<Package> findByTypeNameAndPublishStatus(
            @Param("typeName") TypeName typeName,
            @Param("status") PublishStatus status);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.category = :category AND p.publishStatus = :status")
    List<Package> findByTypeNameAndCategoryAndPublishStatus(
            @Param("typeName") TypeName typeName,
            @Param("category") CategoryType category,
            @Param("status") PublishStatus status);

    @Query("SELECT p FROM Package p WHERE p.type.name = :typeName AND p.pricingType = :pricingType AND p.publishStatus = :status")
    List<Package> findByTypeNameAndPricingTypeAndPublishStatus(
            @Param("typeName") TypeName typeName,
            @Param("pricingType") String pricingType,
            @Param("status") PublishStatus status,
            Pageable pageable);

    @Query("SELECT p FROM Package p WHERE p.publishStatus = :status")
    List<Package> findAllByPublishStatus(@Param("status") PublishStatus status);

    // ── Ticket-holder: PUBLISHED + DRAFT do chính mình tạo ─────────────────────

    @Query("""
            SELECT p FROM Package p
            WHERE p.type.name = :typeName
            AND (p.publishStatus = 'PUBLISHED' OR (p.publishStatus = 'DRAFT' AND p.userId = :userId))
            """)
    List<Package> findByTypeNameForTicketHolder(
            @Param("typeName") TypeName typeName,
            @Param("userId") String userId);

    @Query("""
            SELECT p FROM Package p
            WHERE (p.publishStatus = 'PUBLISHED' OR (p.publishStatus = 'DRAFT' AND p.userId = :userId))
            """)
    List<Package> findAllForTicketHolder(@Param("userId") String userId);

    @Query("""
            SELECT p FROM Package p
            WHERE p.type.name = :typeName
            AND (p.publishStatus = 'PUBLISHED' OR (p.publishStatus = 'DRAFT' AND p.userId = :userId))
            AND p.category = :category
            """)
    List<Package> findByTypeNameAndCategoryForTicketHolder(
            @Param("typeName") TypeName typeName,
            @Param("category") CategoryType category,
            @Param("userId") String userId);
}
