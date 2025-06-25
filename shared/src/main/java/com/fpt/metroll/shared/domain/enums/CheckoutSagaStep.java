package com.fpt.metroll.shared.domain.enums;

public enum CheckoutSagaStep {
    // Forward steps
    VALIDATE_ITEMS,
    CALCULATE_PRICING,
    APPLY_DISCOUNTS,
    CREATE_ORDER,
    PROCESS_PAYMENT,
    GENERATE_TICKETS,
    
    // Compensation steps
    CANCEL_PAYMENT,
    CANCEL_ORDER,
    RELEASE_DISCOUNTS,
    CLEANUP_ITEMS
} 