package com.fpt.metroll.order.document;

import com.fpt.metroll.shared.domain.dto.saga.CheckoutSagaData;
import com.fpt.metroll.shared.domain.enums.CheckoutSagaStep;
import com.fpt.metroll.shared.domain.enums.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "checkout_sagas")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CheckoutSaga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Version
    private Long version;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "order_id")
    private String orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step")
    private CheckoutSagaStep currentStep;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "saga_completed_steps", joinColumns = @JoinColumn(name = "saga_id"))
    @Column(name = "step")
    private List<CheckoutSagaStep> completedSteps;
    
    @Column(name = "saga_data", columnDefinition = "TEXT")
    private String sagaDataJson; // JSON serialized CheckoutSagaData
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "correlation_id")
    private String correlationId;
    
    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "expires_at")
    private Instant expiresAt; // For saga timeout
} 