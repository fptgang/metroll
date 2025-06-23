package com.fpt.metroll.account.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoucherCreateRequest {
    private BigDecimal discountAmount;
    private BigDecimal minTransactionAmount;
    private Instant validFrom;
    private Instant validUntil;
    private List<String> ownerIds;
} 