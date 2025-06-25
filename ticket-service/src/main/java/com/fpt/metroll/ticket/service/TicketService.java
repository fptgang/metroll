package com.fpt.metroll.ticket.service;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.shared.domain.enums.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface TicketService {
    PageDto<TicketDto> findAll(String search, PageableDto pageable);

    Optional<TicketDto> findById(String id);

    TicketDto requireById(String id);

    Optional<TicketDto> findByTicketNumber(String ticketNumber);

    TicketDto requireByTicketNumber(String ticketNumber);

    List<TicketDto> findByOrderDetailId(String orderDetailId);

    List<TicketDto> findByStatus(TicketStatus status);

    void updateStatus(String id, TicketStatus status);

    TicketDto create(TicketUpsertRequest ticketUpsertRequest);
    
    List<TicketDto> createTickets(List<TicketUpsertRequest> ticketRequests);

    String generateQRCodeBase64(String id) throws Exception ;
}