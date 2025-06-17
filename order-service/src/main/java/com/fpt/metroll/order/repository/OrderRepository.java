package com.fpt.metroll.order.repository;

import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.shared.domain.enums.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    List<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, OrderStatus status);
    
    List<Order> findByStaffIdOrderByCreatedAtDesc(String staffId);
    
    List<Order> findByStaffIdAndStatusOrderByCreatedAtDesc(String staffId, OrderStatus status);
    
    Optional<Order> findByTransactionReference(String transactionReference);
} 