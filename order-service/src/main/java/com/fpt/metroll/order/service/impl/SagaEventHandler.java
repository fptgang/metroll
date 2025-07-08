package com.fpt.metroll.order.service.impl;

import com.fpt.metroll.order.service.CheckoutSagaOrchestrator;
import com.fpt.metroll.shared.domain.dto.saga.SagaEvent;
import com.fpt.metroll.shared.domain.enums.CheckoutSagaStep;
import com.fpt.metroll.shared.domain.enums.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class SagaEventHandler {

    private final CheckoutSagaOrchestratorImpl orchestrator;
    private final StreamBridge streamBridge;

    public SagaEventHandler(CheckoutSagaOrchestratorImpl orchestrator, StreamBridge streamBridge) {
        this.orchestrator = orchestrator;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<SagaEvent> handleValidateItems() {
        return event -> {
            if (event.getStep() == CheckoutSagaStep.VALIDATE_ITEMS) {
                log.info("🔍 [EVENT-DRIVEN] Processing VALIDATE_ITEMS for saga {}", event.getSagaId());
                
                // Update status immediately
                orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.IN_PROGRESS);
                
                try {
                    // Simulate validation logic (normally would call validation service)
                    Thread.sleep(100); // Simulate processing time
                    
                    // Validation successful - send success event
                    SagaEvent successEvent = SagaEvent.builder()
                            .sagaId(event.getSagaId())
                            .sagaType("CHECKOUT_SAGA")
                            .step(CheckoutSagaStep.CALCULATE_PRICING)
                            .status(SagaStatus.IN_PROGRESS)
                            .userId(event.getUserId())
                            .payload(Map.of(
                                "validationResult", "SUCCESS",
                                "validatedItems", "sample_items",
                                "checkoutRequest", event.getPayload().get("checkoutRequest")
                            ))
                            .timestamp(Instant.now())
                            .correlationId(event.getCorrelationId())
                            .isCompensation(false)
                            .build();
                    
                    // Send next step event
                    streamBridge.send("saga.calculate_pricing", successEvent);
                    log.info("✅ [EVENT-DRIVEN] VALIDATE_ITEMS completed for saga {}, sent CALCULATE_PRICING", event.getSagaId());
                    
                } catch (Exception e) {
                    log.error("❌ [EVENT-DRIVEN] VALIDATE_ITEMS failed for saga {}: {}", event.getSagaId(), e.getMessage());
                    
                    // Send failure event
                    SagaEvent failureEvent = SagaEvent.builder()
                            .sagaId(event.getSagaId())
                            .sagaType("CHECKOUT_SAGA")
                            .step(event.getStep())
                            .status(SagaStatus.FAILED)
                            .userId(event.getUserId())
                            .error("Validation failed: " + e.getMessage())
                            .timestamp(Instant.now())
                            .correlationId(event.getCorrelationId())
                            .isCompensation(false)
                            .build();
                    
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.FAILED);
                    // In real implementation, would send to dead letter queue or retry
                }
            }
        };
    }

    @Bean
    public Consumer<SagaEvent> handleCalculatePricing() {
        return event -> {
            if (event.getStep() == CheckoutSagaStep.CALCULATE_PRICING) {
                log.info("💰 [EVENT-DRIVEN] Processing CALCULATE_PRICING for saga {}", event.getSagaId());
                
                try {
                    // Simulate pricing calculation
                    Thread.sleep(150);
                    
                    SagaEvent successEvent = SagaEvent.builder()
                            .sagaId(event.getSagaId())
                            .sagaType("CHECKOUT_SAGA")
                            .step(CheckoutSagaStep.APPLY_DISCOUNTS)
                            .status(SagaStatus.IN_PROGRESS)
                            .userId(event.getUserId())
                            .payload(Map.of(
                                "baseTotal", "25.50",
                                "calculationResult", "SUCCESS",
                                "checkoutRequest", event.getPayload().get("checkoutRequest")
                            ))
                            .timestamp(Instant.now())
                            .correlationId(event.getCorrelationId())
                            .isCompensation(false)
                            .build();
                    
                    streamBridge.send("saga.apply_discounts", successEvent);
                    log.info("✅ [EVENT-DRIVEN] CALCULATE_PRICING completed for saga {}, sent APPLY_DISCOUNTS", event.getSagaId());
                    
                } catch (Exception e) {
                    log.error("❌ [EVENT-DRIVEN] CALCULATE_PRICING failed for saga {}: {}", event.getSagaId(), e.getMessage());
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.FAILED);
                }
            }
        };
    }

    @Bean
    public Consumer<SagaEvent> handleApplyDiscounts() {
        return event -> {
            if (event.getStep() == CheckoutSagaStep.APPLY_DISCOUNTS) {
                log.info("🎟️ [EVENT-DRIVEN] Processing APPLY_DISCOUNTS for saga {}", event.getSagaId());
                
                try {
                    // Simulate discount application
                    Thread.sleep(120);
                    
                    SagaEvent successEvent = SagaEvent.builder()
                            .sagaId(event.getSagaId())
                            .sagaType("CHECKOUT_SAGA")
                            .step(CheckoutSagaStep.CREATE_ORDER)
                            .status(SagaStatus.IN_PROGRESS)
                            .userId(event.getUserId())
                            .payload(Map.of(
                                "baseTotal", event.getPayload().get("baseTotal"),
                                "discountTotal", "2.50",
                                "finalTotal", "23.00",
                                "checkoutRequest", event.getPayload().get("checkoutRequest")
                            ))
                            .timestamp(Instant.now())
                            .correlationId(event.getCorrelationId())
                            .isCompensation(false)
                            .build();
                    
                    streamBridge.send("saga.create_order", successEvent);
                    log.info("✅ [EVENT-DRIVEN] APPLY_DISCOUNTS completed for saga {}, sent CREATE_ORDER", event.getSagaId());
                    
                } catch (Exception e) {
                    log.error("❌ [EVENT-DRIVEN] APPLY_DISCOUNTS failed for saga {}: {}", event.getSagaId(), e.getMessage());
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.FAILED);
                }
            }
        };
    }

    @Bean
    public Consumer<SagaEvent> handleCreateOrder() {
        return event -> {
            if (event.getStep() == CheckoutSagaStep.CREATE_ORDER) {
                log.info("📋 [EVENT-DRIVEN] Processing CREATE_ORDER for saga {}", event.getSagaId());
                
                try {
                    // Simulate order creation
                    Thread.sleep(200);
                    String orderId = "order_" + System.currentTimeMillis();
                    
                    SagaEvent successEvent = SagaEvent.builder()
                            .sagaId(event.getSagaId())
                            .sagaType("CHECKOUT_SAGA")
                            .step(CheckoutSagaStep.PROCESS_PAYMENT)
                            .status(SagaStatus.IN_PROGRESS)
                            .userId(event.getUserId())
                            .orderId(orderId)
                            .payload(Map.of(
                                "orderId", orderId,
                                "finalTotal", event.getPayload().get("finalTotal"),
                                "orderCreated", "SUCCESS"
                            ))
                            .timestamp(Instant.now())
                            .correlationId(event.getCorrelationId())
                            .isCompensation(false)
                            .build();
                    
                    streamBridge.send("saga.process_payment", successEvent);
                    log.info("✅ [EVENT-DRIVEN] CREATE_ORDER completed for saga {}, created order {}, sent PROCESS_PAYMENT", 
                        event.getSagaId(), orderId);
                    
                } catch (Exception e) {
                    log.error("❌ [EVENT-DRIVEN] CREATE_ORDER failed for saga {}: {}", event.getSagaId(), e.getMessage());
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.FAILED);
                }
            }
        };
    }

    @Bean
    public Consumer<SagaEvent> handleProcessPayment() {
        return event -> {
            if (event.getStep() == CheckoutSagaStep.PROCESS_PAYMENT) {
                log.info("💳 [EVENT-DRIVEN] Processing PROCESS_PAYMENT for saga {}", event.getSagaId());
                
                try {
                    // Simulate payment processing
                    Thread.sleep(300);
                    String paymentId = "payment_" + System.currentTimeMillis();
                    
                    SagaEvent successEvent = SagaEvent.builder()
                            .sagaId(event.getSagaId())
                            .sagaType("CHECKOUT_SAGA")
                            .step(CheckoutSagaStep.GENERATE_TICKETS)
                            .status(SagaStatus.IN_PROGRESS)
                            .userId(event.getUserId())
                            .orderId(event.getOrderId())
                            .payload(Map.of(
                                "paymentId", paymentId,
                                "orderId", event.getPayload().get("orderId"),
                                "paymentStatus", "SUCCESS"
                            ))
                            .timestamp(Instant.now())
                            .correlationId(event.getCorrelationId())
                            .isCompensation(false)
                            .build();
                    
                    streamBridge.send("saga.generate_tickets", successEvent);
                    log.info("✅ [EVENT-DRIVEN] PROCESS_PAYMENT completed for saga {}, payment {}, sent GENERATE_TICKETS", 
                        event.getSagaId(), paymentId);
                    
                } catch (Exception e) {
                    log.error("❌ [EVENT-DRIVEN] PROCESS_PAYMENT failed for saga {}: {}", event.getSagaId(), e.getMessage());
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.FAILED);
                }
            }
        };
    }

    @Bean
    public Consumer<SagaEvent> handleGenerateTickets() {
        return event -> {
            if (event.getStep() == CheckoutSagaStep.GENERATE_TICKETS) {
                log.info("🎫 [EVENT-DRIVEN] Processing GENERATE_TICKETS for saga {}", event.getSagaId());
                
                try {
                    // Simulate ticket generation
                    Thread.sleep(250);
                    String ticketId = "ticket_" + System.currentTimeMillis();
                    
                    // Final step - mark saga as COMPLETED
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.COMPLETED);
                    
                    log.info("🎉 [EVENT-DRIVEN] GENERATE_TICKETS completed for saga {}, ticket {}, SAGA COMPLETED!", 
                        event.getSagaId(), ticketId);
                    
                } catch (Exception e) {
                    log.error("❌ [EVENT-DRIVEN] GENERATE_TICKETS failed for saga {}: {}", event.getSagaId(), e.getMessage());
                    orchestrator.updateSagaStatus(event.getSagaId(), SagaStatus.FAILED);
                }
            }
        };
    }
} 