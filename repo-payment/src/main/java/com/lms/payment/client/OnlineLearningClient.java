package com.lms.payment.client;

import com.lms.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "repo-onl-learning")
public interface OnlineLearningClient {

    @GetMapping("/courses/count")
    ApiResponse<Long> getOnlineCourseCount();
}