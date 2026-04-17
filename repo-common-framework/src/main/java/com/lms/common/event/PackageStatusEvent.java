package com.lms.common.event;

/**
 * Kafka event phát ra khi trạng thái publish của Package thay đổi.
 *
 * <ul>
 *   <li>{@code status} — "DRAFT" hoặc "PUBLISHED"</li>
 *   <li>{@code reason} — "CONTENT_UPDATED" | "ADMIN_PUBLISH" | "ADMIN_UNPUBLISH"</li>
 * </ul>
 *
 * <p>Các service lắng nghe event này để cập nhật UI hoặc gửi notification
 * cho người dùng, nhưng KHÔNG được xóa progress của người học.</p>
 */
public record PackageStatusEvent(
        String packageId,
        String packageName,
        String moduleType,   // TicketModuleEnum name, e.g. "FLASHCARD"
        String status,       // "DRAFT" | "PUBLISHED"
        String changedBy,    // userId thực hiện hành động
        String reason        // "CONTENT_UPDATED" | "ADMIN_PUBLISH" | "ADMIN_UNPUBLISH"
) {
}
