package com.fpt.metroll.shared.domain.dto.order;

import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private String id;
    private String orderId;

    private List<String> ticketIds;
    
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