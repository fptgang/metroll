package com.fpt.metroll.account.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoucherUpdateRequest {
    private BigDecimal discountAmount;
    private BigDecimal minTransactionAmount;
    private Instant validFrom;
    private Instant validUntil;
} 