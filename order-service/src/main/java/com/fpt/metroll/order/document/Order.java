package com.fpt.metroll.order.document;

import com.fpt.metroll.shared.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "ticket_orders")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Nhân viên giúp mua vé offline
    @Column(name = "staff_id")
    private String staffId;
    
    // Khách hàng, có thể null nếu nhân viên ko nhập
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "discount_package")
    private String discountPackage; // Reference to AccountDiscountPackage._id
    
    @Column(name = "voucher")
    private String voucher; // Reference to Voucher._id
    
    @Column(name = "base_total", precision = 19, scale = 2)
    private BigDecimal baseTotal; // tổng giá
    
    @Column(name = "discount_total", precision = 19, scale = 2)
    private BigDecimal discountTotal; // tổng discount
    
    @Column(name = "final_total", precision = 19, scale = 2)
    private BigDecimal finalTotal; // tổng cuối cùng
    
    @Column(name = "payment_method")
    private String paymentMethod; // CASH|VNPAY|PAYOS
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status; // PENDING|COMPLETED|FAILED
    
    @Column(name = "transaction_reference")
    private String transactionReference;
    
    // PayOS payment information
    @Column(name = "payment_url", length = 1000)
    private String paymentUrl; // Payment link for PayOS
    
    @Column(name = "qr_code", length = 1000)
    private String qrCode; // QR code for PayOS payment
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
    
        @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
} 