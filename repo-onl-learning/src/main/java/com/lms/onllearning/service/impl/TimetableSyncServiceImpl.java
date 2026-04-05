package com.lms.onllearning.service.impl;

import com.lms.onllearning.client.CmsClient;
import com.lms.onllearning.dto.response.CmsCourseTimetableResponse;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.repository.TimetableSnapshotRepository;
import com.lms.onllearning.service.ITimetableSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableSyncServiceImpl implements ITimetableSyncService {

    private static final String ROLE_STUDENT = "STUDENT";

    private final CmsClient cmsClient;
    private final TimetableSnapshotRepository timetableSnapshotRepository;

    @Value("${timetable.sync.batch-size:500}")
    private int batchSize;

    @Override
    @Transactional
    public void syncAll() {
        CmsEnvelope<List<CmsCourseTimetableResponse>> envelope = cmsClient.getCoursesTimetableEmails();
        if (envelope.meta().cmsUnavailable() || envelope.data() == null) {
            log.warn("[TimetableSync] CMS không khả dụng, bỏ qua syncAll.");
            return;
        }

        long syncVersion = System.currentTimeMillis();
        SnapshotRows rows = flattenSnapshot(envelope.data(), syncVersion);

        timetableSnapshotRepository.batchUpsertClasses(rows.classes(), batchSize);
        timetableSnapshotRepository.batchUpsertSessions(rows.sessions(), batchSize);
        timetableSnapshotRepository.batchUpsertParticipants(rows.participants(), batchSize);
        timetableSnapshotRepository.cleanupOldSnapshot(syncVersion);

        log.info("[TimetableSync] Sync hoàn tất. classes={}, sessions={}, participants={}, syncVersion={}",
                rows.classes().size(), rows.sessions().size(), rows.participants().size(), syncVersion);
    }

    private SnapshotRows flattenSnapshot(List<CmsCourseTimetableResponse> source, long syncVersion) {
        List<Object[]> classRows = new ArrayList<>();
        List<Object[]> sessionRows = new ArrayList<>();
        List<Object[]> participantRows = new ArrayList<>();
        Set<String> participantDedupe = new HashSet<>();

        for (CmsCourseTimetableResponse course : safeList(source)) {
            if (course == null || isBlank(course.id())) {
                continue;
            }

            for (CmsCourseTimetableResponse.ClassItem clazz : safeList(course.classes())) {
                if (clazz == null || isBlank(clazz.classId())) {
                    continue;
                }

                String classId = trim(clazz.classId());
                classRows.add(new Object[] {
                        classId,
                        trimToNull(clazz.classCode()),
                        trimToNull(clazz.className()),
                        trimToNull(clazz.classStatus()),
                        trimToNull(clazz.startDate()),
                        trimToNull(clazz.endDate()),
                        syncVersion
                });

                collectParticipants(participantRows, participantDedupe, classId, ROLE_STUDENT,
                        clazz.relatedEmails() != null ? clazz.relatedEmails().studentEmails() : null, syncVersion);

                for (CmsCourseTimetableResponse.SessionItem session : safeList(clazz.timetable())) {
                    if (session == null || isBlank(session.sessionId())) {
                        continue;
                    }

                    String sessionClassId = isBlank(session.classId()) ? classId : trim(session.classId());
                    sessionRows.add(new Object[] {
                            trim(session.sessionId()),
                            sessionClassId,
                            trimToNull(session.syllabusScheduleId()),
                            session.sessionNo(),
                            trimToNull(session.title()),
                            trimToNull(session.date()),
                            trimToNull(session.sessionDate()),
                            trimToNull(session.startTime()),
                            trimToNull(session.endTime()),
                            trimToNull(session.status()),
                            trimToNull(session.sessionStatus()),
                            session.isExam(),
                            trimToNull(session.tutorId()),
                            normalizeEmail(session.tutorEmail()),
                            trimToNull(session.tutorName()),
                            trimToNull(session.instructorName()),
                            trimToNull(session.classRoom()),
                            trimToNull(session.attendanceStatus()),
                            trimToNull(session.checkInAt()),
                            trimToNull(session.examId()),
                            trimToNull(session.examType()),
                            trimToNull(session.examTitle()),
                            trimToNull(session.examDatetime()),
                            trimToNull(session.examLink()),
                            trimToNull(session.examNote()),
                            syncVersion
                    });
                }
            }
        }

        return new SnapshotRows(classRows, sessionRows, participantRows);
    }

    private void collectParticipants(List<Object[]> targetRows,
            Set<String> dedupe,
            String classId,
            String role,
            List<String> emails,
            long syncVersion) {
        for (String rawEmail : safeList(emails)) {
            String email = normalizeEmail(rawEmail);
            if (email == null) {
                continue;
            }

            String dedupeKey = classId + "|" + role + "|" + email;
            if (!dedupe.add(dedupeKey)) {
                continue;
            }

            targetRows.add(new Object[] { classId, email, role, syncVersion });
        }
    }

    private static <T> List<T> safeList(List<T> source) {
        return source == null ? List.of() : source;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private record SnapshotRows(
            List<Object[]> classes,
            List<Object[]> sessions,
            List<Object[]> participants) {
    }
}
