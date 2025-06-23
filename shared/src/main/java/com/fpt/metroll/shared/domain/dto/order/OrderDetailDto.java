package com.fpt.metroll.shared.domain.dto.order;

import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
public class OrderDetailDto {
    private String id;
    private String ticketOrderId;
    
    private TicketType ticketType; // P2P|TIMED
    private String p2pJourney; // Reference to P2PJourney._id
    private String timedTicketPlan; // Reference to TimedTicketPlan._id
    
    private Integer quantity;
    private BigDecimal unitPrice; // giá đơn vị
    private BigDecimal baseTotal; // tổng giá
    private BigDecimal discountTotal; // tổng discount
    private BigDecimal finalTotal; // tổng cuối cùng
    
    private Instant createdAt;
} 