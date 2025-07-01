package com.fpt.metroll.shared.domain.dto.discount;

import com.fpt.metroll.shared.domain.enums.AccountDiscountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDiscountPackageDto {
    private String id;
    private String accountId;
    private String discountPackageId;
    private Instant activateDate;
    private Instant validUntil;
    private AccountDiscountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
} 