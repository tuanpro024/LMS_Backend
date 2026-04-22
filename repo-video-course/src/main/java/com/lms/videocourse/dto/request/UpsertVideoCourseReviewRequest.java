package com.lms.videocourse.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertVideoCourseReviewRequest {

    @NotBlank(message = "packageId không được trống")
    private String packageId;

    @Min(value = 1, message = "Rating tối thiểu là 1")
    @Max(value = 5, message = "Rating tối đa là 5")
    private int rating;

    @Size(max = 2000, message = "Feedback tối đa 2000 ký tự")
    private String feedback;
}
