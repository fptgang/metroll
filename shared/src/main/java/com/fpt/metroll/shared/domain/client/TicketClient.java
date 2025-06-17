package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "ticket-service")
public interface TicketClient {

    @PostMapping("/ticket/")
    TicketDto createTicket(TicketUpsertRequest ticketUpsertRequest);
    
    @GetMapping("/p2p-journeys/{id}")
    P2PJourneyDto getP2PJourneyById(@PathVariable("id") String id);
    
    @GetMapping("/timed-ticket-plans/{id}")
    TimedTicketPlanDto getTimedTicketPlanById(@PathVariable("id") String id);
    
    @PostMapping("/ticket/batch")
    List<TicketDto> createTickets(List<TicketUpsertRequest> ticketRequests);
}