package com.lms.onllearning.service;

import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;

import java.util.List;

public interface ISyllabusService {
    CmsEnvelope<List<SyllabusResponse>> getAllSyllabuses();
    CmsEnvelope<SyllabusDetailResponse> getSyllabusDetail(String syllabusId);
}
