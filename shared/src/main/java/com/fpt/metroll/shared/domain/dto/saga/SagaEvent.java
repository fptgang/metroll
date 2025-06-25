package com.fpt.metroll.shared.domain.dto.saga;

import com.fpt.metroll.shared.domain.enums.CheckoutSagaStep;
import com.fpt.metroll.shared.domain.enums.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {
    private String sagaId;
    private String sagaType; // "CHECKOUT_SAGA"
    private CheckoutSagaStep step;
    private SagaStatus status;
    private String orderId;
    private String userId;
    private Map<String, Object> payload; // Step-specific data
    private String error; // Error message if step failed
    private Instant timestamp;
    private String correlationId; // For tracing
    
    // For compensation tracking
    private boolean isCompensation;
    private CheckoutSagaStep compensatingStep;
} 