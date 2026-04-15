package com.lms.payment.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.payment.entity.MembershipPlan;
import com.lms.payment.repository.MembershipPlanRepository;
import com.lms.payment.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository repository;

    @Override
    public List<MembershipPlan> getActivePlans() {
        return repository.findAllByActiveTrueOrderByPriceAsc();
    }

    @Override
    public List<MembershipPlan> getAllPlans() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public MembershipPlan createPlan(MembershipPlan plan) {
        return repository.save(plan);
    }

    @Override
    @Transactional
    public MembershipPlan updatePlan(String id, MembershipPlan plan) {
        MembershipPlan existing = getById(id);
        existing.setName(plan.getName());
        existing.setDescription(plan.getDescription());
        existing.setPrice(plan.getPrice());
        existing.setDurationInDays(plan.getDurationInDays());
        existing.setActive(plan.getActive());
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void deletePlan(String id) {
        repository.deleteById(id);
    }

    @Override
    public MembershipPlan getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Membership plan not found"));
    }
}
