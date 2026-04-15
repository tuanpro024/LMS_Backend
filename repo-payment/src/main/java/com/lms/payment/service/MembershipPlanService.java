package com.lms.payment.service;

import com.lms.payment.entity.MembershipPlan;
import java.util.List;

public interface MembershipPlanService {
    List<MembershipPlan> getActivePlans();
    List<MembershipPlan> getAllPlans();
    MembershipPlan createPlan(MembershipPlan plan);
    MembershipPlan updatePlan(String id, MembershipPlan plan);
    void deletePlan(String id);
    MembershipPlan getById(String id);
}
