package com.fpt.metroll.order.document;

import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_details")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "ticket_id")
    private String ticketId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type")
    private TicketType ticketType; // P2P|TIMED
    
    @Column(name = "p2p_journey")
    private String p2pJourney; // Reference to P2PJourney._id
    
    @Column(name = "timed_ticket_plan")
    private String timedTicketPlan; // Reference to TimedTicketPlan._id
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice; // giá đơn vị
    
    @Column(name = "base_total", precision = 19, scale = 2)
    private BigDecimal baseTotal; // tổng giá
    
    @Column(name = "discount_total", precision = 19, scale = 2)
    private BigDecimal discountTotal; // tổng discount
    
    @Column(name = "final_total", precision = 19, scale = 2)
    private BigDecimal finalTotal; // tổng cuối cùng
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
} 