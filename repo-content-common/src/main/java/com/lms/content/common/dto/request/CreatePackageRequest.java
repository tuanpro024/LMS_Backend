package com.lms.content.common.dto.request;

import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required")
    private TypeName type;

    private String description;

    private String thumbnail;

    private CategoryType category;

    private BigDecimal price;

    private String pricingType;
}
