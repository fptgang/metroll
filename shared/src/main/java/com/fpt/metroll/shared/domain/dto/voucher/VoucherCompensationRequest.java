package com.fpt.metroll.shared.domain.dto.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherCompensationRequest {
    private String compensationId; // cancelled ticket Id
    private String fromStationName;
    private String toStationName;
    private String userId;
    private BigDecimal discountAmount;
    private BigDecimal minTransactionAmount;
    private Instant validFrom;
    private Instant validUntil;
}
