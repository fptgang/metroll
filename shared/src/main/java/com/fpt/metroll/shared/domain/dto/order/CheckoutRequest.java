package com.fpt.metroll.shared.domain.dto.order;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    private List<CheckoutItemRequest> items;
    private String paymentMethod; // CASH|VNPAY|PAYOS
    
    // Optional fields for discounts
//    private String discountPackageId; // Reference to AccountDiscountPackage._id
    private String voucherId; // Reference to Voucher._id
    
    // For offline purchases by staff
    private String customerId; // Customer ID (optional for staff purchases)
} 