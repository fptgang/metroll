package com.fpt.metroll.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.metroll.order.config.PayOSConfig;
import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.order.document.OrderDetail;
import com.fpt.metroll.order.repository.OrderRepository;
import com.fpt.metroll.order.service.PayOSService;
import com.fpt.metroll.shared.domain.client.TicketClient;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
import com.fpt.metroll.shared.domain.enums.OrderStatus;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.shared.exception.PaymentProcessingException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.client.VoucherClient;

@Service
@Slf4j
public class PayOSServiceImpl implements PayOSService {
    
    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final TicketClient ticketClient;
    private final VoucherClient voucherClient;

    public PayOSServiceImpl(PayOSConfig payOSConfig,
                           OrderRepository orderRepository,
                           ObjectMapper objectMapper,
                           @Autowired(required = false) PayOS payOS,
                            TicketClient ticketClient,
                            VoucherClient voucherClient
                            ) {
        this.payOS = payOS;
        this.payOSConfig = payOSConfig;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.ticketClient = ticketClient;
        this.voucherClient = voucherClient;
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
                orderRepository.save(order);
                createTicketsForOrder(order);
                
                // Mark voucher as used if voucher was applied
                if (order.getVoucher() != null && !order.getVoucher().isEmpty()) {
                    try {
                        SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                            voucherClient.use(order.getVoucher());
                        });
                        log.info("Voucher {} marked as used for completed order {}", order.getVoucher(), order.getId());
                    } catch (Exception e) {
                        log.error("Failed to mark voucher {} as used for order {}: {}", 
                            order.getVoucher(), order.getId(), e.getMessage());
                        // Note: Order is still completed, but voucher state may be inconsistent
                        // This should be handled by a cleanup job
                    }
                }
                
                log.info("Payment completed for order {} with amount {}", 
                        order.getId(), webhookData.getAmount());
            } else {
                // Payment failed
                order.setStatus(OrderStatus.FAILED);
                
                // Unpreserve voucher if voucher was applied
                if (order.getVoucher() != null && !order.getVoucher().isEmpty()) {
                    try {
                        SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                            voucherClient.unpreserve(order.getVoucher());
                        });
                        log.info("Voucher {} unpreserved for failed order {}", order.getVoucher(), order.getId());
                    } catch (Exception e) {
                        log.error("Failed to unpreserve voucher {} for failed order {}: {}", 
                            order.getVoucher(), order.getId(), e.getMessage());
                        // Voucher may remain in PRESERVED state - needs cleanup job
                    }
                }
                
                log.warn("Payment failed for order {} with code {} and description {}", 
                        order.getId(), webhookData.getCode(), webhookData.getDesc());
            }
            
            orderRepository.save(order);
            
        } catch (Exception e) {
            log.error("Failed to process payment completion: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process payment completion: " + e.getMessage(), e);
        }
    }
    private void createTicketsForOrder(Order order) {
        List<TicketUpsertRequest> ticketRequests = new ArrayList<>();

        for (OrderDetail detail : order.getOrderDetails()) {
//            for (int i = 0; i < detail.getQuantity(); i++) {
            Instant validUntil = calculateValidUntil(detail);

            TicketUpsertRequest ticketRequest = TicketUpsertRequest.builder()
                                                                   .ticketType(detail.getTicketType())
                                                                   .ticketOrderDetailId(detail.getId())
                                                                   .validUntil(validUntil)
                                                                   .status(TicketStatus.VALID)
                                                                   .build();

            ticketRequests.add(ticketRequest);
//            }
        }

        if (!ticketRequests.isEmpty()) {
            try {
                List<TicketDto> createdTickets = ticketClient.createTickets(ticketRequests);
                Map<String, String> ticketIdMap = createdTickets.stream()
                                                                .collect(Collectors.toMap(TicketDto::getTicketOrderDetailId, TicketDto::getId));

                order.getOrderDetails().forEach(orderDetail -> {
                    String ticketId = ticketIdMap.get(orderDetail.getId());
                    if (ticketId != null) {
                        orderDetail.setTicketId(ticketId);
                    }
                });
                orderRepository.save(order);
                log.info("Created {} tickets for order {}", createdTickets.size(), order.getId());
            } catch (Exception e) {
                log.error("Failed to create tickets for order {}", order.getId(), e);
                // In a real system, this might trigger a compensation flow
            }
        }
    }
    private Instant calculateValidUntil(OrderDetail detail) {
        if (detail.getTicketType() == TicketType.P2P) {
            // P2P tickets are valid for 1 day
            return Instant.now().plus(1, ChronoUnit.DAYS);
        } else if (detail.getTicketType() == TicketType.TIMED && detail.getTimedTicketPlan() != null) {
            // Get the plan duration from the ticket service
            try {
                TimedTicketPlanDto plan = ticketClient.getTimedTicketPlanById(detail.getTimedTicketPlan());
                return Instant.now().plus(plan.getValidDuration(), ChronoUnit.DAYS);
            } catch (Exception e) {
                log.warn("Failed to get timed ticket plan duration for {}, defaulting to 30 days",
                        detail.getTimedTicketPlan());
                return Instant.now().plus(30, ChronoUnit.DAYS);
            }
        }
        return Instant.now().plus(1, ChronoUnit.DAYS);
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