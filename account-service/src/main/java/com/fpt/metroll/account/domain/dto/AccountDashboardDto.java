package com.fpt.metroll.account.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDashboardDto {
    private Long totalAccounts;
    private Map<String, Long> accountsByRole;
    private Long activeAccounts;
    private Long inactiveAccounts;
    private Long staffWithAssignedStation;
    private Long staffWithoutAssignedStation;
    private Long totalDiscountPackages;
    private Long activeDiscountPackages;
    private Long totalVouchers;
    private BigDecimal totalVoucherValue;
    private Instant lastUpdated;
}