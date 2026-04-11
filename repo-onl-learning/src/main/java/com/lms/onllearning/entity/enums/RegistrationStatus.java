package com.lms.onllearning.entity.enums;

/**
 * Trạng thái đăng ký tư vấn khóa học online.
 * <ul>
 *   <li>PENDING_SALES – đã gửi phiếu, đang chờ bộ phận tư vấn/sales xác nhận ngoài hệ thống</li>
 *   <li>APPROVED     – đã được kích hoạt (thủ công bởi Admin/Manager, hoặc tự động qua CMS sync)</li>
 *   <li>REJECTED     – bị từ chối (không đủ điều kiện, hủy, v.v.)</li>
 * </ul>
 */
public enum RegistrationStatus {
    PENDING_SALES,
    APPROVED,
    REJECTED
}
