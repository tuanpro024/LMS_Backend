package com.lms.notification.service;
import com.lms.common.notification.NotificationEvent;
import com.lms.notification.dto.response.NotificationPageResponse;
import com.lms.notification.dto.response.NotificationResponse;

public interface NotificationService {
    NotificationResponse create(NotificationEvent request);
    NotificationPageResponse list(String userId, int page, int size);
    NotificationResponse markSeen(String userId, String notificationId);
    NotificationResponse markRead(String userId, String notificationId);
    void markAllSeen(String userId);
    long unreadCount(String userId);
}
