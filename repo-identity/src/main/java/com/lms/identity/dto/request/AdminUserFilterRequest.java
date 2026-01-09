package com.lms.identity.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import com.lms.identity.entity.UserStatus;

@Getter
@Setter
public class AdminUserFilterRequest {
    private String emailContains;
    private String role;
    private UserStatus status;

    @Min(value = 0, message = "Input Parameter Error. Invalid data value. (page)")
    private int page = 0;

    @Min(value = 1, message = "Input Parameter Error. Invalid data value. (size)")
    private int size = 20;
}
