package com.lms.onllearning.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TimetableSnapshotRepository {

    private static final String UPSERT_CLASS_SQL = """
            INSERT INTO timetable_classes
                (id, class_code, class_name, class_status, start_date, end_date, sync_version)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                class_code = VALUES(class_code),
                class_name = VALUES(class_name),
                class_status = VALUES(class_status),
                start_date = VALUES(start_date),
                end_date = VALUES(end_date),
                sync_version = VALUES(sync_version),
                updated_at = CURRENT_TIMESTAMP(6)
            """;

    private static final String UPSERT_SESSION_SQL = """
            INSERT INTO timetable_sessions (
                id, class_id, syllabus_schedule_id, session_no, title,
                date, session_date, start_time, end_time, status, session_status,
                is_exam, tutor_id, tutor_email, tutor_name, instructor_name,
                class_room, attendance_status, check_in_at,
                exam_id, exam_type, exam_title, exam_datetime, exam_link, exam_note,
                sync_version
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                class_id = VALUES(class_id),
                syllabus_schedule_id = VALUES(syllabus_schedule_id),
                session_no = VALUES(session_no),
                title = VALUES(title),
                date = VALUES(date),
                session_date = VALUES(session_date),
                start_time = VALUES(start_time),
                end_time = VALUES(end_time),
                status = VALUES(status),
                session_status = VALUES(session_status),
                is_exam = VALUES(is_exam),
                tutor_id = VALUES(tutor_id),
                tutor_email = VALUES(tutor_email),
                tutor_name = VALUES(tutor_name),
                instructor_name = VALUES(instructor_name),
                class_room = VALUES(class_room),
                attendance_status = VALUES(attendance_status),
                check_in_at = VALUES(check_in_at),
                exam_id = VALUES(exam_id),
                exam_type = VALUES(exam_type),
                exam_title = VALUES(exam_title),
                exam_datetime = VALUES(exam_datetime),
                exam_link = VALUES(exam_link),
                exam_note = VALUES(exam_note),
                sync_version = VALUES(sync_version),
                updated_at = CURRENT_TIMESTAMP(6)
            """;

    private static final String UPSERT_PARTICIPANT_SQL = """
            INSERT INTO timetable_class_participants (class_id, email, participant_role, sync_version)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                sync_version = VALUES(sync_version),
                updated_at = CURRENT_TIMESTAMP(6)
            """;

    private static final String CLEANUP_PARTICIPANT_SQL = "DELETE FROM timetable_class_participants WHERE sync_version <> ?";
    private static final String CLEANUP_SESSION_SQL = "DELETE FROM timetable_sessions WHERE sync_version <> ?";
    private static final String CLEANUP_CLASS_SQL = "DELETE FROM timetable_classes WHERE sync_version <> ?";

    private final JdbcTemplate jdbcTemplate;

    public void batchUpsertClasses(List<Object[]> rows, int batchSize) {
        batchUpsert(UPSERT_CLASS_SQL, rows, batchSize);
    }

    public void batchUpsertSessions(List<Object[]> rows, int batchSize) {
        batchUpsert(UPSERT_SESSION_SQL, rows, batchSize);
    }

    public void batchUpsertParticipants(List<Object[]> rows, int batchSize) {
        batchUpsert(UPSERT_PARTICIPANT_SQL, rows, batchSize);
    }

    public void cleanupOldSnapshot(long syncVersion) {
        jdbcTemplate.update(CLEANUP_PARTICIPANT_SQL, syncVersion);
        jdbcTemplate.update(CLEANUP_SESSION_SQL, syncVersion);
        jdbcTemplate.update(CLEANUP_CLASS_SQL, syncVersion);
    }

    private void batchUpsert(String sql, List<Object[]> rows, int batchSize) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        int safeBatchSize = batchSize > 0 ? batchSize : 500;
        for (int start = 0; start < rows.size(); start += safeBatchSize) {
            int end = Math.min(start + safeBatchSize, rows.size());
            jdbcTemplate.batchUpdate(sql, rows.subList(start, end));
        }
    }
}
