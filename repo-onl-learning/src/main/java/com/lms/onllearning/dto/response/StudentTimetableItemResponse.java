package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps item response từ CMS API:
 * GET /api/erp/students/{studentId}/timetable?start=yyyy-MM-dd&end=yyyy-MM-dd
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StudentTimetableItemResponse(
        String id,
        @JsonProperty("class_id") String classId,
        @JsonProperty("class_code") String classCode,
        @JsonProperty("class_name") String className,
        @JsonProperty("session_no") Integer sessionNo,
        String title,
        String date,
        @JsonProperty("session_date") String sessionDate,
        @JsonProperty("start_time") String startTime,
        @JsonProperty("end_time") String endTime,
        String status,
        @JsonProperty("tutor_id") String tutorId,
        @JsonProperty("tutor_name") String tutorName,
        @JsonProperty("instructor_name") String instructorName,
        @JsonProperty("class_room") String classRoom,
        @JsonProperty("attendance_status") String attendanceStatus,
        @JsonProperty("check_in_at") String checkInAt
) {
}
