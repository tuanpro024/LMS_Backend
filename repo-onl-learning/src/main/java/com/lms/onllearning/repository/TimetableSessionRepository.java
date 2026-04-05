package com.lms.onllearning.repository;

import com.lms.onllearning.entity.TimetableSession;
import com.lms.onllearning.repository.projection.TimetableSessionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimetableSessionRepository extends JpaRepository<TimetableSession, String> {

    @Query(value = """
            SELECT DISTINCT
                s.id AS id,
                c.id AS classId,
                s.syllabus_schedule_id AS syllabusScheduleId,
                c.class_code AS classCode,
                c.class_name AS className,
                s.session_no AS sessionNo,
                s.title AS title,
                s.date AS date,
                s.session_date AS sessionDate,
                s.start_time AS startTime,
                s.end_time AS endTime,
                s.status AS status,
                s.session_status AS sessionStatus,
                s.is_exam AS isExam,
                s.tutor_id AS tutorId,
                s.tutor_email AS tutorEmail,
                s.tutor_name AS tutorName,
                s.instructor_name AS instructorName,
                s.class_room AS classRoom,
                s.attendance_status AS attendanceStatus,
                s.check_in_at AS checkInAt,
                s.exam_id AS examId,
                s.exam_type AS examType,
                s.exam_title AS examTitle,
                s.exam_datetime AS examDatetime,
                s.exam_link AS examLink,
                s.exam_note AS examNote
            FROM timetable_sessions s
            JOIN timetable_classes c ON c.id = s.class_id
            JOIN timetable_class_participants p ON p.class_id = c.id
            WHERE p.email = :email
                            AND p.participant_role = 'STUDENT'
            ORDER BY COALESCE(s.session_date, s.date) ASC, s.start_time ASC, s.session_no ASC
            """, nativeQuery = true)
    List<TimetableSessionView> findTimetableByParticipantEmail(@Param("email") String email);
}
