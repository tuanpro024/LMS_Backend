package com.lms.content.common.entity.enums;

/**
 * Trạng thái publish của Package.
 *
 * <ul>
 *   <li>{@link #DRAFT} — Nội dung đang được chỉnh sửa, chỉ admin/manager và
 *       người tạo (ticket-holder) mới thấy.</li>
 *   <li>{@link #PUBLISHED} — Nội dung đã được duyệt, hiển thị cho mọi người dùng.</li>
 * </ul>
 */
public enum PublishStatus {
    DRAFT,
    PUBLISHED
}
