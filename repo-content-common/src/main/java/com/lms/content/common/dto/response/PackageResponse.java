package com.lms.content.common.dto.response;

import com.lms.content.common.entity.CategoryType;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.entity.enums.PublishStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {

    private String id;
    private String name;
    private TypeName type;
    private String description;
    private String thumbnail;
    private CategoryType category;
    private BigDecimal price;
    private String pricingType;
    private String userId;
    private Integer enrollmentCount;
    private PublishStatus publishStatus;
    private List<FolderResponse> folders;
    private Instant createdAt;
    private Instant updatedAt;
}
