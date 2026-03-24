package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;

import java.util.List;

public interface ITimetableService {
    CmsEnvelope<List<StudentTimetableItemResponse>> getStudentTimetable(String email);
}
