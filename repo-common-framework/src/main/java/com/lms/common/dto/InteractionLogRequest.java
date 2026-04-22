package com.lms.common.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for receiving user interaction events from the frontend.
 * Uses a flexible metadata map for event-specific data.
 */
public record InteractionLogRequest(
    String sessionId,
    InteractionEventType eventType,
    Instant timestamp,
    String module,          // e.g. "flashcard", "video-course", "quiz", "ai-practice"
    String targetId,        // ID of the target object (packageId, videoId, quizId, etc.)
    String pageUrl,         // Current page URL path
    Map<String, Object> metadata  // Flexible additional data per event type
) {}
