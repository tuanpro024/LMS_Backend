package com.lms.onllearning.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request để sắp xếp lại thứ tự các module trong một buổi học.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderScheduleModulesRequest {

    @NotNull
    private List<ReorderItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReorderItem {
        @NotNull(message = "Module ID là bắt buộc")
        private String id;

        @NotNull(message = "Thứ tự mới là bắt buộc")
        @Min(value = 1, message = "Thứ tự phải >= 1")
        private Integer newOrder;
    }
}
