package com.lms.onllearning.dto.response;

import com.lms.onllearning.entity.enums.ScheduleModuleType;

import java.time.Instant;
import java.util.List;

public record CourseStudentModuleProgressResponse(
        String courseId,
        String courseCode,
        String courseName,
        String syllabusId,
        Integer totalModules,
        Integer totalProgressRecords,
        List<StudentModuleProgressItem> items) {

    public record StudentModuleProgressItem(
            String moduleId,
            String scheduleId,
            Integer sessionNo,
            Integer moduleOrder,
            String moduleTitle,
            ScheduleModuleType moduleType,
            String userId,
            String userEmail,
            Double progressPercentage,
            Boolean completed,
            Integer completedItems,
            Integer totalItems,
            Instant lastInteractedAt,
            Instant completedAt) {
    }
}
