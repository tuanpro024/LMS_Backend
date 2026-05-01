package com.lms.payment.client;

import com.lms.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "repo-video-course")
public interface VideoCourseClient {

    @GetMapping("/syllabus/courses/count")
    ApiResponse<Long> getVideoCourseCount();
}