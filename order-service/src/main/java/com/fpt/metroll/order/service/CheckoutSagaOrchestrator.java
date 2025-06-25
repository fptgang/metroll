package com.fpt.metroll.order.service;

import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import com.fpt.metroll.shared.domain.dto.saga.SagaEvent;

public interface CheckoutSagaOrchestrator {
    
    /**
     * Start a new checkout saga
     */
    String startCheckoutSaga(CheckoutRequest checkoutRequest, String userId);
    
    /**
     * Process saga event and determine next step
     */
    void processSagaEvent(SagaEvent event);
    
    /**
     * Handle saga step completion
     */
    void handleStepCompletion(String sagaId, String stepResult);
    
    /**
     * Handle saga step failure and initiate compensation
     */
    void handleStepFailure(String sagaId, String error);
    
    /**
     * Get saga status
     */
    String getSagaStatus(String sagaId);
    
    /**
     * Cancel running saga (manual intervention)
     */
    void cancelSaga(String sagaId, String reason);
} 