package com.lms.content.common.dto.request;

import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private TypeName type;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 2048, message = "Thumbnail URL must not exceed 2048 characters")
    private String thumbnail;

    private CategoryType category;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private String pricingType;
}
