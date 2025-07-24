package com.fpt.metroll.shared.domain.enums;

public enum EmailType {
    // Voucher-related emails
    VOUCHER_CLAIMED,
    VOUCHER_REVOKED,
    VOUCHER_EXPIRED,

    // Discount package-related emails
    DISCOUNT_PACKAGE_ASSIGNED,
    DISCOUNT_PACKAGE_UNASSIGNED,
    DISCOUNT_PACKAGE_EXPIRED,

    // Order-related emails
    ORDER_CHECKOUT_SUCCESS,
    ORDER_PAYMENT_CONFIRMATION,
    ORDER_PAYMENT_FAILED,

    TICKET_COMPENSATION,

    // Test email
    TEST_EMAIL
}