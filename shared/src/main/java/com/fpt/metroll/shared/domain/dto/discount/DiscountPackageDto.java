package com.fpt.metroll.shared.domain.dto.discount;

import com.fpt.metroll.shared.domain.enums.DiscountPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountPackageDto {
    private String id;
    private String name;
    private String description;
    private float discountPercentage;
    private Integer duration;
    private DiscountPackageStatus status;
    private Instant createdAt;
    private Instant updatedAt;
} 