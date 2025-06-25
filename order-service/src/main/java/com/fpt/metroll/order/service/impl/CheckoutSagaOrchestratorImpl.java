package com.fpt.metroll.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.metroll.order.document.CheckoutSaga;
import com.fpt.metroll.order.repository.CheckoutSagaRepository;
import com.fpt.metroll.order.service.CheckoutSagaOrchestrator;
import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import com.fpt.metroll.shared.domain.dto.saga.CheckoutSagaData;
import com.fpt.metroll.shared.domain.dto.saga.SagaEvent;
import com.fpt.metroll.shared.domain.enums.CheckoutSagaStep;
import com.fpt.metroll.shared.domain.enums.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CheckoutSagaOrchestratorImpl implements CheckoutSagaOrchestrator {
    
    private final CheckoutSagaRepository sagaRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;
    
    // Saga step workflow definition
    private static final Map<CheckoutSagaStep, CheckoutSagaStep> NEXT_STEPS = Map.of(
        CheckoutSagaStep.VALIDATE_ITEMS, CheckoutSagaStep.CALCULATE_PRICING,
        CheckoutSagaStep.CALCULATE_PRICING, CheckoutSagaStep.APPLY_DISCOUNTS,
        CheckoutSagaStep.APPLY_DISCOUNTS, CheckoutSagaStep.CREATE_ORDER,
        CheckoutSagaStep.CREATE_ORDER, CheckoutSagaStep.PROCESS_PAYMENT,
        CheckoutSagaStep.PROCESS_PAYMENT, CheckoutSagaStep.GENERATE_TICKETS
    );
    
    // Compensation workflow
    private static final Map<CheckoutSagaStep, CheckoutSagaStep> COMPENSATION_STEPS = Map.of(
        CheckoutSagaStep.GENERATE_TICKETS, CheckoutSagaStep.CANCEL_PAYMENT,
        CheckoutSagaStep.PROCESS_PAYMENT, CheckoutSagaStep.CANCEL_ORDER,
        CheckoutSagaStep.CREATE_ORDER, CheckoutSagaStep.RELEASE_DISCOUNTS,
        CheckoutSagaStep.APPLY_DISCOUNTS, CheckoutSagaStep.CLEANUP_ITEMS
    );
    
    public CheckoutSagaOrchestratorImpl(CheckoutSagaRepository sagaRepository,
                                        StreamBridge streamBridge,
                                        ObjectMapper objectMapper) {
        this.sagaRepository = sagaRepository;
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String startCheckoutSaga(CheckoutRequest checkoutRequest, String userId) {
        String sagaId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        
        log.info("Starting EVENT-DRIVEN checkout saga {} for user {}", sagaId, userId);
        
        // Create initial saga event - NO database writes!
        SagaEvent startEvent = SagaEvent.builder()
                .sagaId(sagaId)
                .sagaType("CHECKOUT_SAGA")
                .step(CheckoutSagaStep.VALIDATE_ITEMS)
                .status(SagaStatus.STARTED)
                .userId(userId)
                .payload(Map.of(
                    "checkoutRequest", checkoutRequest,
                    "sagaId", sagaId,
                    "correlationId", correlationId,
                    "userId", userId
                ))
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .isCompensation(false)
                .build();
        
        // Pure event-driven: publish to RabbitMQ immediately
        publishSagaEvent(startEvent);
        
        log.info("Published saga START event {} to RabbitMQ for user {}", sagaId, userId);
        return sagaId;
    }
    
    @Override
    @Transactional
    public void processSagaEvent(SagaEvent event) {
        Optional<CheckoutSaga> sagaOpt = sagaRepository.findById(event.getSagaId());
        if (sagaOpt.isEmpty()) {
            log.warn("Saga not found: {}", event.getSagaId());
            return;
        }
        
        CheckoutSaga saga = sagaOpt.get();
        
        if (event.getStatus() == SagaStatus.COMPLETED) {
            handleStepSuccess(saga, event);
        } else if (event.getStatus() == SagaStatus.FAILED) {
            handleStepFailure(saga.getId(), event.getError());
        }
    }
    
    private void handleStepSuccess(CheckoutSaga saga, SagaEvent event) {
        // Update saga with step completion
        saga.getCompletedSteps().add(event.getStep());
        saga.setStatus(SagaStatus.IN_PROGRESS);
        
        // Update saga data with step results
        CheckoutSagaData sagaData = deserializeSagaData(saga.getSagaDataJson());
        updateSagaDataWithStepResult(sagaData, event);
        saga.setSagaDataJson(serializeSagaData(sagaData));
        
        // Determine next step
        CheckoutSagaStep nextStep = NEXT_STEPS.get(event.getStep());
        
        if (nextStep != null) {
            // Continue to next step
            saga.setCurrentStep(nextStep);
            sagaRepository.save(saga);
            
            SagaEvent nextEvent = SagaEvent.builder()
                    .sagaId(saga.getId())
                    .sagaType("CHECKOUT_SAGA")
                    .step(nextStep)
                    .status(SagaStatus.IN_PROGRESS)
                    .orderId(saga.getOrderId())
                    .userId(saga.getUserId())
                    .payload(createPayloadForStep(nextStep, sagaData))
                    .timestamp(Instant.now())
                    .correlationId(saga.getCorrelationId())
                    .isCompensation(false)
                    .build();
            
            publishSagaEvent(nextEvent);
        } else {
            // Saga completed successfully
            saga.setStatus(SagaStatus.COMPLETED);
            sagaRepository.save(saga);
            log.info("Checkout saga {} completed successfully", saga.getId());
        }
    }
    
    @Override
    @Transactional
    public void handleStepFailure(String sagaId, String error) {
        Optional<CheckoutSaga> sagaOpt = sagaRepository.findById(sagaId);
        if (sagaOpt.isEmpty()) {
            log.warn("Saga not found for failure handling: {}", sagaId);
            return;
        }
        
        CheckoutSaga saga = sagaOpt.get();
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(error);
        
        // Start compensation from the last completed step
        CheckoutSagaStep lastCompletedStep = saga.getCompletedSteps().isEmpty() 
                ? null 
                : saga.getCompletedSteps().get(saga.getCompletedSteps().size() - 1);
        
        if (lastCompletedStep != null) {
            CheckoutSagaStep compensationStep = COMPENSATION_STEPS.get(lastCompletedStep);
            if (compensationStep != null) {
                saga.setCurrentStep(compensationStep);
                sagaRepository.save(saga);
                
                CheckoutSagaData sagaData = deserializeSagaData(saga.getSagaDataJson());
                
                SagaEvent compensationEvent = SagaEvent.builder()
                        .sagaId(sagaId)
                        .sagaType("CHECKOUT_SAGA")
                        .step(compensationStep)
                        .status(SagaStatus.COMPENSATING)
                        .orderId(saga.getOrderId())
                        .userId(saga.getUserId())
                        .payload(createCompensationPayload(compensationStep, sagaData))
                        .timestamp(Instant.now())
                        .correlationId(saga.getCorrelationId())
                        .isCompensation(true)
                        .compensatingStep(lastCompletedStep)
                        .build();
                
                publishSagaEvent(compensationEvent);
                log.info("Started compensation for saga {} at step {}", sagaId, compensationStep);
            } else {
                // No compensation needed, mark as failed
                saga.setStatus(SagaStatus.FAILED);
                sagaRepository.save(saga);
            }
        } else {
            // No steps completed, mark as failed
            saga.setStatus(SagaStatus.FAILED);
            sagaRepository.save(saga);
        }
        
        log.error("Saga {} failed: {}", sagaId, error);
    }
    
    @Override
    public void handleStepCompletion(String sagaId, String stepResult) {
        // This would be called by individual step handlers
        // Implementation depends on specific step requirements
    }
    
    // In-memory saga status tracking (use Redis in production)
    private final Map<String, SagaStatus> sagaStatusMap = new ConcurrentHashMap<>();
    
    @Override
    public String getSagaStatus(String sagaId) {
        // Check in-memory first (event-driven state)
        SagaStatus status = sagaStatusMap.get(sagaId);
        if (status != null) {
            return status.name();
        }
        
        // Fallback to database if needed
        return sagaRepository.findById(sagaId)
                .map(saga -> saga.getStatus().name())
                .orElse("NOT_FOUND");
    }
    
    // Update saga status in memory (called by event handlers)
    public void updateSagaStatus(String sagaId, SagaStatus status) {
        sagaStatusMap.put(sagaId, status);
        log.debug("Updated saga {} status to {}", sagaId, status);
    }
    
    @Override
    @Transactional
    public void cancelSaga(String sagaId, String reason) {
        Optional<CheckoutSaga> sagaOpt = sagaRepository.findById(sagaId);
        if (sagaOpt.isPresent()) {
            CheckoutSaga saga = sagaOpt.get();
            if (saga.getStatus() == SagaStatus.IN_PROGRESS) {
                handleStepFailure(sagaId, "Cancelled: " + reason);
            }
        }
    }
    
    private void publishSagaEvent(SagaEvent event) {
        String routingKey = "saga." + event.getStep().name().toLowerCase();
        streamBridge.send(routingKey, event);
        log.debug("Published saga event: {} for step {}", event.getSagaId(), event.getStep());
    }
    
    private Map<String, Object> createPayloadForStep(CheckoutSagaStep step, CheckoutSagaData sagaData) {
        Map<String, Object> payload = new HashMap<>();
        
        switch (step) {
            case VALIDATE_ITEMS -> payload.put("checkoutRequest", sagaData.getOriginalRequest());
            case CALCULATE_PRICING -> payload.put("validatedItems", sagaData.getValidatedItems());
            case APPLY_DISCOUNTS -> {
                payload.put("baseTotal", sagaData.getBaseTotal());
                payload.put("discountPackageId", sagaData.getOriginalRequest().getDiscountPackageId());
                payload.put("voucherId", sagaData.getOriginalRequest().getVoucherId());
            }
            case CREATE_ORDER -> {
                payload.put("checkoutRequest", sagaData.getOriginalRequest());
                payload.put("finalTotal", sagaData.getFinalTotal());
                payload.put("discountTotal", sagaData.getDiscountTotal());
            }
            case PROCESS_PAYMENT -> {
                payload.put("orderId", sagaData.getOrderId());
                payload.put("paymentMethod", sagaData.getOriginalRequest().getPaymentMethod());
            }
            case GENERATE_TICKETS -> {
                payload.put("orderId", sagaData.getOrderId());
                payload.put("validatedItems", sagaData.getValidatedItems());
            }
        }
        
        return payload;
    }
    
    private Map<String, Object> createCompensationPayload(CheckoutSagaStep compensationStep, CheckoutSagaData sagaData) {
        Map<String, Object> payload = new HashMap<>();
        
        switch (compensationStep) {
            case CANCEL_PAYMENT -> payload.put("paymentId", sagaData.getPaymentId());
            case CANCEL_ORDER -> payload.put("orderId", sagaData.getOrderId());
            case RELEASE_DISCOUNTS -> {
                payload.put("discountPackageId", sagaData.getOriginalRequest().getDiscountPackageId());
                payload.put("voucherId", sagaData.getOriginalRequest().getVoucherId());
            }
            case CLEANUP_ITEMS -> payload.put("validatedItems", sagaData.getValidatedItems());
        }
        
        return payload;
    }
    
    private void updateSagaDataWithStepResult(CheckoutSagaData sagaData, SagaEvent event) {
        Map<String, Object> payload = event.getPayload();
        
        switch (event.getStep()) {
            case VALIDATE_ITEMS -> {
                if (payload.containsKey("validatedItems")) {
                    // Update with validated items
                }
            }
            case CALCULATE_PRICING -> {
                if (payload.containsKey("baseTotal")) {
                    sagaData.setBaseTotal((BigDecimal) payload.get("baseTotal"));
                }
            }
            case APPLY_DISCOUNTS -> {
                if (payload.containsKey("discountTotal")) {
                    sagaData.setDiscountTotal((BigDecimal) payload.get("discountTotal"));
                }
                if (payload.containsKey("finalTotal")) {
                    sagaData.setFinalTotal((BigDecimal) payload.get("finalTotal"));
                }
            }
            case CREATE_ORDER -> {
                if (payload.containsKey("orderId")) {
                    sagaData.setOrderId((String) payload.get("orderId"));
                }
            }
            case PROCESS_PAYMENT -> {
                if (payload.containsKey("paymentId")) {
                    sagaData.setPaymentId((String) payload.get("paymentId"));
                }
            }
            case GENERATE_TICKETS -> {
                if (payload.containsKey("ticketIds")) {
                    sagaData.setTicketIds((List<String>) payload.get("ticketIds"));
                }
            }
        }
    }
    
    private String serializeSagaData(CheckoutSagaData sagaData) {
        try {
            return objectMapper.writeValueAsString(sagaData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize saga data", e);
        }
    }
    
    private CheckoutSagaData deserializeSagaData(String json) {
        try {
            return objectMapper.readValue(json, CheckoutSagaData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize saga data", e);
        }
    }
} 