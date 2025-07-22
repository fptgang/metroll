package com.fpt.metroll.ticket.repository;

import com.fpt.metroll.ticket.document.TimedTicketPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimedTicketPlanRepository extends MongoRepository<TimedTicketPlan, String> {
    boolean existsByName(String name);
    
    // Soft delete support methods
    List<TimedTicketPlan> findByIsActiveTrue();
    Optional<TimedTicketPlan> findByIdAndIsActiveTrue(String id);
    boolean existsByNameAndIsActiveTrue(String name);
    boolean existsByNameAndIsActiveTrueAndIdNot(String name, String id);
}