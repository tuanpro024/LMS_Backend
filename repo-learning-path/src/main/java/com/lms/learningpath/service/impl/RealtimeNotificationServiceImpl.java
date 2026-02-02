package com.lms.learningpath.service.impl;

import com.lms.learningpath.dto.response.ModuleProgressDto;
import com.lms.learningpath.service.IRealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for sending realtime notifications to users via WebSocket
 * Simplified version - can be enhanced with Feign Client later
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeNotificationServiceImpl implements IRealtimeNotificationService {

    @Override
    public void notifyProgressUpdate(String userId, ModuleProgressDto progress) {
        sendNotification(userId, "PROGRESS_UPDATE", progress);
    }

    @Override
    public void notifyModuleCompleted(String userId, ModuleProgressDto progress) {
        sendNotification(userId, "MODULE_COMPLETED", progress);
    }

    @Override
    public void notifyStudySetCompleted(String userId, String studySetId) {
        sendNotification(userId, "STUDY_SET_COMPLETED", Map.of("studySetId", studySetId));
    }

    @Override
    public void notifyStudySetUnlocked(String userId, String studySetId) {
        sendNotification(userId, "STUDY_SET_UNLOCKED", Map.of("studySetId", studySetId));
    }

    private void sendNotification(String userId, String type, Object data) {
        try {
            // TODO: Implement actual WebSocket/Feign Client notification
            log.info("Sending notification [{}] to user {}: {}", type, userId, data);
        } catch (Exception e) {
            log.warn("Failed to send realtime notification [{}]: {}", type, e.getMessage());
        }
    }
}
