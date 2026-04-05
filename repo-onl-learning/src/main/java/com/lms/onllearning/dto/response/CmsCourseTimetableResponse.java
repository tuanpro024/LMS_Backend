package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps response item từ CMS API:
 * GET /api/erp/students/courses-timetable-emails
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CmsCourseTimetableResponse(
        String id,
        String code,
        String name,
        String type,
        String level,
        @JsonProperty("total_lessons") Integer totalLessons,
        List<ClassItem> classes) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClassItem(
            @JsonProperty("class_id") String classId,
            @JsonProperty("class_code") String classCode,
            @JsonProperty("class_name") String className,
            @JsonProperty("class_status") String classStatus,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate,
            List<SessionItem> timetable,
            @JsonProperty("related_emails") RelatedEmails relatedEmails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SessionItem(
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("class_id") String classId,
            @JsonProperty("syllabus_schedule_id") String syllabusScheduleId,
            @JsonProperty("session_no") Integer sessionNo,
            String title,
            String date,
            @JsonProperty("session_date") String sessionDate,
            @JsonProperty("start_time") String startTime,
            @JsonProperty("end_time") String endTime,
            String status,
            @JsonProperty("session_status") String sessionStatus,
            @JsonProperty("is_exam") Integer isExam,
            @JsonProperty("tutor_id") String tutorId,
            @JsonProperty("tutor_email") String tutorEmail,
            @JsonProperty("tutor_name") String tutorName,
            @JsonProperty("instructor_name") String instructorName,
            @JsonProperty("class_room") String classRoom,
            @JsonProperty("attendance_status") String attendanceStatus,
            @JsonProperty("check_in_at") String checkInAt,
            @JsonProperty("exam_id") String examId,
            @JsonProperty("exam_type") String examType,
            @JsonProperty("exam_title") String examTitle,
            @JsonProperty("exam_datetime") String examDatetime,
            @JsonProperty("exam_link") String examLink,
            @JsonProperty("exam_note") String examNote) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RelatedEmails(
            @JsonProperty("student_emails") List<String> studentEmails) {
    }
}
