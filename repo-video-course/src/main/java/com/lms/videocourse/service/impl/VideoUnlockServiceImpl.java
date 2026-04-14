package com.lms.videocourse.service.impl;

import com.lms.videocourse.entity.VideoStep;
import com.lms.videocourse.exception.ResourceNotFoundException;
import com.lms.videocourse.repository.VideoStepRepository;
import com.lms.videocourse.service.IVideoUnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUnlockServiceImpl implements IVideoUnlockService {

    private final VideoStepRepository videoStepRepository;

    @Override
    public boolean isStepUnlocked(String userId, String stepId) {
        videoStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Video step not found: " + stepId));
        return true;
    }

    @Override
    public String getLockReason(String userId, String stepId) {
        return null;
    }
}
