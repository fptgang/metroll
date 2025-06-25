package com.fpt.metroll.ticket.repository;

import com.fpt.metroll.ticket.document.Ticket;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByTicketOrderDetailId(String ticketOrderDetailId);

    List<Ticket> findByStatus(TicketStatus status);

    boolean existsByTicketNumber(String ticketNumber);
}