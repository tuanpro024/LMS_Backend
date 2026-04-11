package com.lms.onllearning.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body đăng ký tư vấn khóa học.
 * userId KHÔNG có trong request — lấy từ JWT ở backend.
 */
public record LeadRegistrationRequest(

        @NotBlank(message = "Mã khóa học không được để trống") String code,

        @NotBlank(message = "Tên khóa học không được để trống") String name,

        @NotBlank(message = "Loại khóa học không được để trống") String courseType,

        @NotBlank(message = "Vui lòng nhập họ và tên") @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự") String fullName,

        @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không hợp lệ") String email,

        @NotBlank(message = "Vui lòng nhập số điện thoại") @Pattern(regexp = "^(0|\\+84)[0-9]{8,10}$", message = "Số điện thoại không hợp lệ") String phone,

        @Size(max = 500, message = "Ghi chú không quá 500 ký tự") String note) {
}
