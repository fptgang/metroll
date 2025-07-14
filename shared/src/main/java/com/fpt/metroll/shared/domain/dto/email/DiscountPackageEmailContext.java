package com.fpt.metroll.shared.domain.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountPackageEmailContext {

    private String discountPackageId;
    private String discountPackageName;
    private String discountPackageDescription;
    private Double discountPercentage;
    private Instant validFrom;
    private Instant validUntil;
    private String status;
    private String actionPerformedBy; // Staff/Admin who performed the action
    private Instant actionDate;
    private String actionReason; // Optional reason for assignment/unassignment

}