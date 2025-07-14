package com.fpt.metroll.shared.domain.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailContext {

    private String orderId;
    private String transactionReference;
    private BigDecimal baseTotal;
    private BigDecimal discountTotal;
    private BigDecimal finalTotal;
    private String paymentMethod;
    private String status;
    private Instant orderDate;
    private List<OrderItemContext> orderItems;
    private String voucherCode;
    private String discountPackageName;
    private String staffName; // If order was created by staff
    private String paymentUrl; // For PayOS payments

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemContext {
        private String ticketType;
        private String description; // Journey or plan description
        private BigDecimal unitPrice;
        private BigDecimal baseTotal;
        private BigDecimal discountTotal;
        private BigDecimal finalTotal;
        private String ticketId;
        private Instant validUntil;
    }

}