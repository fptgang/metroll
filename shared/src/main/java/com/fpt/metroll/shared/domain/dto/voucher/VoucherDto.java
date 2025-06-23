package com.fpt.metroll.shared.domain.dto.voucher;

import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class VoucherDto {
    private String id;
    private String ownerId;
    private String code;
    private Double discountAmount;
    private Double minTransactionAmount;
    private Instant validFrom;
    private Instant validUntil;
    private VoucherStatus status;
    private Instant createdAt;
    private Instant updatedAt;
} 