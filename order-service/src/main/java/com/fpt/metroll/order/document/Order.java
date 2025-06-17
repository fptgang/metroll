package com.fpt.metroll.order.document;

import com.fpt.metroll.shared.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "ticket_orders")
@Builder
@Data
public class Order {
    @Id
    private String id;
    
    // Nhân viên giúp mua vé offline
    private String staffId;
    
    // Khách hàng, có thể null nếu nhân viên ko nhập
    private String customerId;
    
    private String discountPackage; // Reference to AccountDiscountPackage._id
    private String voucher; // Reference to Voucher._id
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal baseTotal; // tổng giá
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal discountTotal; // tổng discount
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal finalTotal; // tổng cuối cùng
    
    private String paymentMethod; // CASH|VNPAY|PAYOS
    private OrderStatus status; // PENDING|COMPLETED|FAILED
    private String transactionReference;
    
    // PayOS payment information
    private String paymentUrl; // Payment link for PayOS
    private String qrCode; // QR code for PayOS payment
    
    private List<OrderDetail> orderDetails;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
} 