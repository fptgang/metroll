package com.fpt.metroll.shared.domain.dto.saga;

import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSagaData {
    // Original request
    private CheckoutRequest originalRequest;
    
    // Step results
    private List<ValidatedItem> validatedItems;
    private BigDecimal baseTotal;
    private BigDecimal discountTotal;
    private BigDecimal finalTotal;
    private String orderId;
    private String paymentId;
    private List<String> ticketIds;
    
    // For compensation
    private Map<String, Object> compensationData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidatedItem {
        private String itemId;
        private String ticketType;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal total;
    }
} 