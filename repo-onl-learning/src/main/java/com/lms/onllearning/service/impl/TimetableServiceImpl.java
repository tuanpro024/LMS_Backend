package com.lms.onllearning.service.impl;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
import com.lms.onllearning.repository.TimetableSessionRepository;
import com.lms.onllearning.repository.projection.TimetableSessionView;
import com.lms.onllearning.service.ITimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TimetableServiceImpl implements ITimetableService {

    private final TimetableSessionRepository timetableSessionRepository;

    @Override
    public CmsEnvelope<List<StudentTimetableItemResponse>> getStudentTimetable(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return CmsEnvelope.noScheduleFromDb(List.of());
        }

        List<StudentTimetableItemResponse> rows = timetableSessionRepository
                .findTimetableByParticipantEmail(normalizedEmail)
                .stream()
                .map(this::toResponse)
                .toList();

        if (rows.isEmpty()) {
            return CmsEnvelope.noScheduleFromDb(List.of());
        }
        return CmsEnvelope.fromDb(rows);
    }

    private StudentTimetableItemResponse toResponse(TimetableSessionView item) {
        String mergedStatus = item.getSessionStatus() != null ? item.getSessionStatus() : item.getStatus();
        return new StudentTimetableItemResponse(
                item.getId(),
                item.getClassId(),
                item.getSyllabusScheduleId(),
                item.getClassCode(),
                item.getClassName(),
                item.getSessionNo(),
                item.getTitle(),
                item.getDate(),
                item.getSessionDate(),
                item.getStartTime(),
                item.getEndTime(),
                mergedStatus,
                item.getSessionStatus(),
                toIntFlag(item.getIsExam()),
                item.getTutorId(),
                item.getTutorEmail(),
                item.getTutorName(),
                item.getInstructorName(),
                item.getClassRoom(),
                item.getAttendanceStatus(),
                item.getCheckInAt(),
                item.getExamId(),
                item.getExamType(),
                item.getExamTitle(),
                item.getExamDatetime(),
                item.getExamLink(),
                item.getExamNote());
    }

    private Integer toIntFlag(Boolean value) {
        if (value == null) {
            return null;
        }
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
