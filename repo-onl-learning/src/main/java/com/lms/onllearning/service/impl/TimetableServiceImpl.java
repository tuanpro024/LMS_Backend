package com.lms.onllearning.service.impl;

import com.lms.onllearning.client.CmsClient;
import com.lms.onllearning.dto.response.CmsEnvelope;
import com.lms.onllearning.dto.response.StudentTimetableItemResponse;
import com.lms.onllearning.service.ITimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableServiceImpl implements ITimetableService {

    private final CmsClient cmsClient;

    @Override
    public CmsEnvelope<List<StudentTimetableItemResponse>> getStudentTimetable(String email) {
        return cmsClient.getStudentTimetable(email);
    }
}
