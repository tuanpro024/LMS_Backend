package com.lms.learningpath.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderItemsRequest {

    @NotNull(message = "Items are required")
    private List<ReorderItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReorderItem {
        @NotNull
        private String id;

        @NotNull
        private Integer newOrder;
    }
}
