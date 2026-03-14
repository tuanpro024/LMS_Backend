package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.TimetableResponse;

import java.util.List;

public interface ITimetableService {
    CmsEnvelope<List<TimetableResponse>> getMyTimetable(String userId);
}
