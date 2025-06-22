package com.fpt.metroll.shared.domain.enums;

public enum OrderStatus {
    PENDING,    // Order created but not paid
    COMPLETED,  // Order completed and tickets generated
    FAILED      // Order payment failed
} 