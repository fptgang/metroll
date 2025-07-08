package com.fpt.metroll.shared.domain.enums;

public enum SagaStatus {
    STARTED,        // Saga initiated
    IN_PROGRESS,    // Executing saga steps
    COMPLETED,      // All steps completed successfully
    FAILED,         // Saga failed at some step
    COMPENSATING,   // Running compensation actions
    COMPENSATED     // Compensation completed
} 