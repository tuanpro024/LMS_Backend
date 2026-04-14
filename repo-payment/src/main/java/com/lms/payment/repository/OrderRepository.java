package com.lms.payment.repository;

import com.lms.payment.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lms.payment.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Order> findByOrderCode(Long orderCode);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalPriceByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(DISTINCT o.userId) FROM Order o WHERE o.status = :status")
    long countDistinctUserIdByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(i) FROM Order o JOIN o.items i WHERE o.status = :status")
    long countTotalItemsByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(i) FROM Order o JOIN o.items i WHERE o.status = :status AND i.itemType = :itemType")
    long countTotalItemsByStatusAndItemType(@Param("status") OrderStatus status, @Param("itemType") com.lms.payment.entity.enums.ItemType itemType);

    Page<Order> findAll(Pageable pageable);
}
