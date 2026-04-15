package com.lms.payment.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.payment.entity.MembershipPlan;
import com.lms.payment.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/membership-plans")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService planService;

    // API công khai cho người dùng lựa chọn gói
    @GetMapping
    public ApiResponse<List<MembershipPlan>> getActivePlans() {
        return ApiResponse.ok(planService.getActivePlans());
    }

    // API cho Admin quản lý
    @GetMapping("/admin/all")
    public ApiResponse<List<MembershipPlan>> getAllPlans() {
        return ApiResponse.ok(planService.getAllPlans());
    }

    @PostMapping("/admin")
    public ApiResponse<MembershipPlan> createPlan(@RequestBody MembershipPlan plan) {
        return ApiResponse.ok(planService.createPlan(plan));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<MembershipPlan> updatePlan(@PathVariable String id, @RequestBody MembershipPlan plan) {
        return ApiResponse.ok(planService.updatePlan(id, plan));
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deletePlan(@PathVariable String id) {
        planService.deletePlan(id);
        return ApiResponse.ok(null);
    }
}
