package com.fpt.metroll.shared.domain.dto.email;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCompensationEmailContext {
    private String cancelledTicketId; //ticket id
    private String fromStationName;
    private String toStationName;
    private String voucherCode;
    private BigDecimal discountAmount;
    private BigDecimal minTransactionAmount;
    private Instant validFrom;
    private Instant validUntil;
}
