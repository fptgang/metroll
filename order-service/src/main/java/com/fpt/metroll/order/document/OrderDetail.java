package com.fpt.metroll.order.document;

import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
public class OrderDetail {
    private String id;
    
    private TicketType ticketType; // P2P|TIMED
    
    private String p2pJourney; // Reference to P2PJourney._id
    private String timedTicketPlan; // Reference to TimedTicketPlan._id
    
    private Integer quantity;
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal unitPrice; // giá đơn vị
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal baseTotal; // tổng giá
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal discountTotal; // tổng discount
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal finalTotal; // tổng cuối cùng
    
    @CreatedDate
    private Instant createdAt;
} 