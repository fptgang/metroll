package com.fpt.metroll.order.service;

import com.fpt.metroll.order.document.Order;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

public interface PayOSService {
    
    /**
     * Create payment link for an order
     */
    CheckoutResponseData createPaymentLink(Order order);
    
    /**
     * Get payment link information
     */
    PaymentLinkData getPaymentLinkInfo(Long orderCode);
    
    /**
     * Cancel payment link
     */
    PaymentLinkData cancelPaymentLink(Long orderCode, String reason);
    
    /**
     * Confirm webhook URL with PayOS
     */
    String confirmWebhook(String webhookUrl);
    
    /**
     * Process payment completion
     */
    void processPaymentCompletion(Webhook webhookData);
} 