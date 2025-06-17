package com.fpt.metroll.shared.domain.dto.order;

import com.fpt.metroll.shared.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Data
public class OrderDto {
    private String id;
    
    // Nhân viên giúp mua vé offline
    private String staffId;
    
    // Khách hàng, có thể null nếu nhân viên ko nhập
    private String customerId;
    
    private String discountPackage; // Reference to AccountDiscountPackage._id
    private String voucher; // Reference to Voucher._id
    
    private BigDecimal baseTotal; // tổng giá
    private BigDecimal discountTotal; // tổng discount
    private BigDecimal finalTotal; // tổng cuối cùng
    
    private String paymentMethod; // CASH|VNPAY|PAYOS
    private OrderStatus status; // PENDING|COMPLETED|FAILED
    private String transactionReference;
    
    // PayOS payment information
    private String paymentUrl; // Payment link for PayOS
    private String qrCode; // QR code for PayOS payment
    
    private List<OrderDetailDto> orderDetails;
    private Instant createdAt;
    private Instant updatedAt;
} 