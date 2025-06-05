package com.fpt.metroll.ticket.repository;

import com.fpt.metroll.ticket.document.TimedTicketPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimedTicketPlanRepository extends MongoRepository<TimedTicketPlan, String> {
    boolean existsByName(String name);
}