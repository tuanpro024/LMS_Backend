package com.lms.payment.repository;

import com.lms.payment.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, String> {
    List<MembershipPlan> findAllByActiveTrueOrderByPriceAsc();
}
