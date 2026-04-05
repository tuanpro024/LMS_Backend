package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Item timetable trả cho frontend, lấy từ DB snapshot.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StudentTimetableItemResponse(
                String id,
                @JsonProperty("class_id") String classId,
                @JsonProperty("syllabus_schedule_id") String syllabusScheduleId,
                @JsonProperty("class_code") String classCode,
                @JsonProperty("class_name") String className,
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
