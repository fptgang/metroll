package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "ticket-service")
public interface TicketClient {

    @PostMapping("/ticket/")
    TicketDto createTicket(TicketUpsertRequest ticketUpsertRequest);
}