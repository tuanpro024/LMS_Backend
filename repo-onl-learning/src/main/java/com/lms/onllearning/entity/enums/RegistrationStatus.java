package com.lms.onllearning.entity.enums;

/**
 * Trạng thái đăng ký tư vấn khóa học online.
 * <ul>
 *   <li>PENDING     – đã gửi phiếu, đang chờ tư vấn liên hệ</li>
 *   <li>IN_PROGRESS – đã có thời khóa biểu từ CMS, đang học</li>
 *   <li>COMPLETED   – đã hoàn thành khóa học, có thể đăng ký lại</li>
 *   <li>CANCELED    – quá 3 ngày PENDING mà không có thời khóa biểu, có thể đăng ký lại</li>
 * </ul>
 */
public enum RegistrationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELED
}
