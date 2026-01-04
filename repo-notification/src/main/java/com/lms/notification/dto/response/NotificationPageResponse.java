package com.lms.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationPageResponse {
    private final List<NotificationResponse> items;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;
    private final long unreadCount;
}