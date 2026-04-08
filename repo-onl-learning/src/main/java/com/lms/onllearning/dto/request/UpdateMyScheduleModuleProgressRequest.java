package com.lms.onllearning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMyScheduleModuleProgressRequest {

    @DecimalMin(value = "0.0", message = "progressPercentage phải >= 0")
    @DecimalMax(value = "100.0", message = "progressPercentage phải <= 100")
    private Double progressPercentage;

    private Boolean completed;

    private Integer completedItems;

    private Integer totalItems;
}
