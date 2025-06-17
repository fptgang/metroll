package com.fpt.metroll.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.metroll.order.config.PayOSConfig;
import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.order.document.OrderDetail;
import com.fpt.metroll.order.repository.OrderRepository;
import com.fpt.metroll.order.service.PayOSService;
import com.fpt.metroll.shared.domain.enums.OrderStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
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
        if (payOS == null) {
            log.warn("PayOS not configured, skipping payment link creation for order {}", order.getId());
            return createMockPaymentResponse(order);
        }
        
        try {
            // Convert order to PayOS format
            Long orderCode = Long.parseLong(order.getId().replace("-", "").substring(0, 12));
            
            // Create items from order details
            List<ItemData> items = order.getOrderDetails().stream()
                    .map(this::convertOrderDetailToItem)
                    .collect(Collectors.toList());
            
            // Build payment data
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(order.getFinalTotal().intValue())
                    .description("Thanh toán đơn hàng Metro #" + order.getId())
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
            log.error("Failed to create PayOS payment link for order {}", order.getId(), e);
            
            // Update order with mock payment info
            order.setTransactionReference("MOCK-" + System.currentTimeMillis());
            order.setPaymentMethod("PAYOS");
            order.setPaymentUrl("http://localhost:8080/mock-payment/" + order.getId());
            order.setQrCode("mock-qr-code");
            orderRepository.save(order);
            
            return createMockPaymentResponse(order);
        }
    }
    
    @Override
    public PaymentLinkData getPaymentLinkInfo(Long orderCode) {
        if (payOS == null) {
            log.warn("PayOS not configured");
            return null;
        }
        
        try {
            return payOS.getPaymentLinkInformation(orderCode);
        } catch (Exception e) {
            log.error("Failed to get payment link info for order code {}", orderCode, e);
            return null;
        }
    }
    
    @Override
    public PaymentLinkData cancelPaymentLink(Long orderCode, String reason) {
        if (payOS == null) {
            log.warn("PayOS not configured");
            return null;
        }
        
        try {
            return payOS.cancelPaymentLink(orderCode, reason);
        } catch (Exception e) {
            log.error("Failed to cancel payment link for order code {}", orderCode, e);
            return null;
        }
    }
    
    @Override
    public WebhookData verifyWebhookData(String webhookBody) {
        if (payOS == null) {
            log.warn("PayOS not configured");
            return null;
        }
        
        try {
            Webhook webhook = objectMapper.readValue(webhookBody, Webhook.class);
            return payOS.verifyPaymentWebhookData(webhook);
        } catch (Exception e) {
            log.error("Failed to verify webhook data", e);
            return null;
        }
    }
    
    @Override
    public void processPaymentCompletion(WebhookData webhookData) {
        try {
            // Extract order ID from transaction reference
            String transactionRef = "PAYOS-" + webhookData.getOrderCode();
            Order order = orderRepository.findByTransactionReference(transactionRef)
                    .orElse(null);
            
            if (order == null) {
                log.error("Order not found for transaction reference: {}", transactionRef);
                return;
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
            log.error("Failed to process payment completion", e);
        }
    }
    
    private ItemData convertOrderDetailToItem(OrderDetail detail) {
        String itemName = buildItemName(detail);
        
        return ItemData.builder()
                .name(itemName)
                .quantity(detail.getQuantity())
                .price(detail.getUnitPrice().intValue())
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
    
    private CheckoutResponseData createMockPaymentResponse(Order order) {
        // Create a mock response when PayOS is not configured
        // Since CheckoutResponseData doesn't have a public constructor or builder,
        // we'll return null and handle it in the calling code
        log.info("PayOS not configured, returning mock response for order {}", order.getId());
        return null;
    }
} 