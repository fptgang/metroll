package com.fpt.metroll.order.repository;

import com.fpt.metroll.order.document.CheckoutSaga;
import com.fpt.metroll.shared.domain.enums.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckoutSagaRepository extends JpaRepository<CheckoutSaga, String> {
    
    Optional<CheckoutSaga> findByOrderId(String orderId);
    
    List<CheckoutSaga> findByUserIdAndStatusIn(String userId, List<SagaStatus> statuses);
    
    List<CheckoutSaga> findByStatus(SagaStatus status);
    
    @Query("SELECT s FROM CheckoutSaga s WHERE s.expiresAt < :now AND s.status IN :activeStatuses")
    List<CheckoutSaga> findExpiredSagas(Instant now, List<SagaStatus> activeStatuses);
    
    @Query("SELECT s FROM CheckoutSaga s WHERE s.status = :status AND s.updatedAt < :before")
    List<CheckoutSaga> findStuckSagas(SagaStatus status, Instant before);
} 