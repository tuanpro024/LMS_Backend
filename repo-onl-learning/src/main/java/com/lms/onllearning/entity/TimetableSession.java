package com.lms.onllearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "timetable_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSession {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @Column(name = "class_id", length = 40, nullable = false)
    private String classId;

    @Column(name = "syllabus_schedule_id", length = 26)
    private String syllabusScheduleId;

    @Column(name = "session_no")
    private Integer sessionNo;

    @Column(length = 255)
    private String title;

    @Column(name = "date", length = 40)
    private String date;

    @Column(name = "session_date", length = 40)
    private String sessionDate;

    @Column(name = "start_time", length = 20)
    private String startTime;

    @Column(name = "end_time", length = 20)
    private String endTime;

    @Column(length = 50)
    private String status;

    @Column(name = "session_status", length = 50)
    private String sessionStatus;

    @Column(name = "is_exam")
    private Boolean isExam;

    @Column(name = "tutor_id", length = 40)
    private String tutorId;

    @Column(name = "tutor_email", length = 255)
    private String tutorEmail;

    @Column(name = "tutor_name", length = 255)
    private String tutorName;

    @Column(name = "instructor_name", length = 255)
    private String instructorName;

    @Column(name = "class_room", length = 500)
    private String classRoom;

    @Column(name = "attendance_status", length = 50)
    private String attendanceStatus;

    @Column(name = "check_in_at", length = 40)
    private String checkInAt;

    @Column(name = "exam_id", length = 40)
    private String examId;

    @Column(name = "exam_type", length = 100)
    private String examType;

    @Column(name = "exam_title", length = 255)
    private String examTitle;

    @Column(name = "exam_datetime", length = 40)
    private String examDatetime;

    @Column(name = "exam_link", length = 500)
    private String examLink;

    @Column(name = "exam_note", columnDefinition = "TEXT")
    private String examNote;

    @Column(name = "sync_version", nullable = false)
    private Long syncVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
