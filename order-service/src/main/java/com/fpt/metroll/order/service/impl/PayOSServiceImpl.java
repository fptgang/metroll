package com.fpt.metroll.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.metroll.order.config.PayOSConfig;
import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.order.document.OrderDetail;
import com.fpt.metroll.order.repository.OrderRepository;
import com.fpt.metroll.order.service.PayOSService;
import com.fpt.metroll.shared.domain.enums.OrderStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.shared.exception.PaymentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PayOSServiceImpl implements PayOSService {
    
    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    
    public PayOSServiceImpl(PayOSConfig payOSConfig,
                           OrderRepository orderRepository,
                           ObjectMapper objectMapper,
                           @Autowired(required = false) PayOS payOS) {
        this.payOS = payOS;
        this.payOSConfig = payOSConfig;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public CheckoutResponseData createPaymentLink(Order order) {
        // Validate PayOS configuration
        if (payOS == null) {
            log.error("PayOS not configured. Cannot process payment for order {}", order.getId());
            throw new PaymentProcessingException("PayOS payment gateway is not configured. Please contact administrator.");
        }
        
        try {
            // Convert order to PayOS format
            Long orderCode = generateOrderCode(order.getId());
            
            // Create items from order details
            List<ItemData> items = order.getOrderDetails().stream()
                    .map(this::convertOrderDetailToItem)
                    .collect(Collectors.toList());
            
            // Ensure amount is in cents/smallest currency unit and at least 1 cent
            int amount = Math.max(1, order.getFinalTotal().intValue());
            
            // Ensure description is within 25 character limit
            String description = "Metro #" + order.getId().substring(0, Math.min(8, order.getId().length()));
            
            // Build payment data
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .items(items)
                    .returnUrl(payOSConfig.getWebhookUrl() + "/success?orderId=" + order.getId())
                    .cancelUrl(payOSConfig.getWebhookUrl() + "/cancel?orderId=" + order.getId())
                    .build();
            
            CheckoutResponseData response = payOS.createPaymentLink(paymentData);
            
            // Update order with PayOS information
            order.setTransactionReference("PAYOS-" + orderCode);
            order.setPaymentMethod("PAYOS");
            order.setPaymentUrl(response.getCheckoutUrl());
            order.setQrCode(response.getQrCode());
            orderRepository.save(order);
            
            log.info("Created PayOS payment link for order {} with order code {}", order.getId(), orderCode);
            return response;
            
        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for order {}: {}", order.getId(), e.getMessage(), e);
            
            // Update order status to FAILED
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            
            // Throw specific payment processing exception with PayOS identifier
            throw new PaymentProcessingException("Failed to create PayOS payment link: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PaymentLinkData getPaymentLinkInfo(Long orderCode) {
        if (payOS == null) {
            throw new PaymentProcessingException("PayOS not configured");
        }
        
        try {
            return payOS.getPaymentLinkInformation(orderCode);
        } catch (Exception e) {
            log.error("Failed to get payment link info for order code {}: {}", orderCode, e.getMessage(), e);
            throw new PaymentProcessingException("Failed to get payment link information: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PaymentLinkData cancelPaymentLink(Long orderCode, String reason) {
        if (payOS == null) {
            throw new PaymentProcessingException("PayOS not configured");
        }
        
        try {
            return payOS.cancelPaymentLink(orderCode, reason);
        } catch (Exception e) {
            log.error("Failed to cancel payment link for order code {}: {}", orderCode, e.getMessage(), e);
            throw new PaymentProcessingException("Failed to cancel payment link: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String confirmWebhook(String webhookUrl) {
        try {
            log.info("Confirming PayOS webhook URL: {}", webhookUrl);
            var webHookConfirmed = payOS.confirmWebhook(webhookUrl);
            log.info("PayOS webhook confirmed successfully for URL: {}", webhookUrl);
            return webHookConfirmed;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to confirm PayOS webhook: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to confirm webhook: " + e.getMessage());
        }
    }
    
    @Override
    public void processPaymentCompletion(Webhook webhook) {
        try {

            var webhookData = payOS.verifyPaymentWebhookData(webhook);

            // Extract order ID from transaction reference
            String transactionRef = "PAYOS-" + webhookData.getOrderCode();
            Order order = orderRepository.findByTransactionReference(transactionRef)
                    .orElse(null);
            
            if (order == null) {
                log.error("Order not found for transaction reference: {}", transactionRef);
                throw new PaymentProcessingException("Order not found for transaction reference: " + transactionRef);
            }
            
            // Update order status based on webhook data
            if ("00".equals(webhookData.getCode())) {
                // Payment successful
                order.setStatus(OrderStatus.COMPLETED);
                log.info("Payment completed for order {} with amount {}", 
                        order.getId(), webhookData.getAmount());
            } else {
                // Payment failed
                order.setStatus(OrderStatus.FAILED);
                log.warn("Payment failed for order {} with code {} and description {}", 
                        order.getId(), webhookData.getCode(), webhookData.getDesc());
            }
            
            orderRepository.save(order);
            
        } catch (Exception e) {
            log.error("Failed to process payment completion: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process payment completion: " + e.getMessage(), e);
        }
    }
    
    private Long generateOrderCode(String orderId) {
        try {
            // Convert UUID to numeric order code by taking the first 12 digits
            String numericString = orderId.replace("-", "").replaceAll("[^0-9]", "");
            if (numericString.length() < 12) {
                // Pad with zeros if needed
                numericString = String.format("%-12s", numericString).replace(' ', '0');
            }
            return Long.parseLong(numericString.substring(0, 12));
        } catch (NumberFormatException e) {
            // Fallback: use timestamp + random number
            return System.currentTimeMillis() % 1000000000000L;
        }
    }
    
    private ItemData convertOrderDetailToItem(OrderDetail detail) {
        String itemName = buildItemName(detail);
        
        // Ensure price is in cents/smallest currency unit and at least 1 cent
        int priceInCents = Math.max(1, detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(100)).intValue());
        
        return ItemData.builder()
                .name(itemName)
                .quantity(1)
                .price(priceInCents)
                .build();
    }
    
    private String buildItemName(OrderDetail detail) {
        if (detail.getTicketType() == TicketType.P2P) {
            return String.format("Vé P2P - %s", detail.getP2pJourney());
        } else if (detail.getTicketType() == TicketType.TIMED) {
            return String.format("Vé Thời gian - %s", detail.getTimedTicketPlan());
        }
        return "Vé Metro";
    }
} 