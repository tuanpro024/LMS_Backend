package com.lms.writing.dto.request;

import com.lms.content.common.entity.enums.ContentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWordStatusRequest {

    @NotNull(message = "Status is required")
    private ContentStatus status;
}
