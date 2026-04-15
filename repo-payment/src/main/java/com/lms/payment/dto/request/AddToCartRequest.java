package com.lms.payment.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private String packageId;
    private String packageName;
    private java.math.BigDecimal price;
    private String thumbnail;
    private com.lms.payment.entity.enums.ItemType itemType;
}
