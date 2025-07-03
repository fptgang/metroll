package com.fpt.metroll.ticket.service;

import com.fpt.metroll.shared.domain.dto.firebase.TicketStatusFirebaseDto;
import com.fpt.metroll.shared.domain.enums.FirebaseTicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;

import java.time.Instant;
import java.util.Optional;

public interface FirebaseTicketStatusService {

    /**
     * Create ticket status in Firebase when ticket is created
     */
    void createTicketStatus(String ticketId, TicketType ticketType, TicketStatus status, Instant validUntil,
            String orderDetailId);

    /**
     * Update ticket status in Firebase after validation
     */
    void updateTicketStatusAfterValidation(String ticketId, TicketType ticketType, TicketStatus databaseStatus);

    /**
     * Get ticket status from Firebase
     */
    Optional<TicketStatusFirebaseDto> getTicketStatus(String ticketId);

    /**
     * Remove ticket status from Firebase (cleanup)
     */
    void removeTicketStatus(String ticketId);
}