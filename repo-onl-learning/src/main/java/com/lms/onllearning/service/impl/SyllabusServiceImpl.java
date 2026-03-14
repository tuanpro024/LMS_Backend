package com.lms.onllearning.service.impl;

import com.lms.onllearning.client.CmsClient;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.SyllabusDetailResponse;
import com.lms.onllearning.dto.response.SyllabusResponse;
import com.lms.onllearning.service.ISyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyllabusServiceImpl implements ISyllabusService {

    private final CmsClient cmsClient;

    @Override
    public CmsEnvelope<List<SyllabusResponse>> getAllSyllabuses() {
        return cmsClient.getSyllabuses();
    }

    @Override
    public CmsEnvelope<SyllabusDetailResponse> getSyllabusDetail(String syllabusId) {
        return cmsClient.getSyllabusDetail(syllabusId);
    }
}
