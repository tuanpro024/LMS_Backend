package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;

import java.time.LocalDate;
import java.util.List;

public interface ITimetableService {
    CmsEnvelope<List<StudentTimetableItemResponse>> getStudentTimetable(String studentId, LocalDate start, LocalDate end);
}
