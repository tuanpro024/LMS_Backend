package com.lms.onllearning.dto.response;

import com.lms.onllearning.entity.enums.ScheduleModuleType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ScheduleSessionProgressResponse(
        CourseInfo course,
        SessionInfo session,
        List<ModuleProgressItem> moduleProgresses,
        HomeworkSummary homeworkSummary,
        ProgressLock progressLock
) {

    public record CourseInfo(
            String id,
            String code,
            String name,
            String level,
            String syllabusId,
            BigDecimal price,
            double rating,
            String thumbnail
    ) {
    }

    public record SessionInfo(
            String scheduleId,
            Integer sessionNo,
            String topic,
            String content
    ) {
    }

    public record ModuleProgressItem(
            String moduleId,
            Integer moduleOrder,
            String title,
            ScheduleModuleType moduleType,
            String contentSetId,
            Double progressPercentage,
            boolean completed,
            String progressSource,
            String message
    ) {
    }

    public record HomeworkSummary(
            int requiredModuleCount,
            double completedPercentageSum,
            double totalPercentage,
            double completionPercent,
            String completionText
    ) {
    }

    public record ProgressLock(
            boolean locked,
            String nextScheduleId,
            Integer nextSessionNo,
            Instant nextSessionStartAt,
            String reason
    ) {
    }
}