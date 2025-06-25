package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Component;

import java.util.List;

@FeignClient(name = "ticket-service", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class, fallbackFactory = TicketClient.TicketClientFallbackFactory.class)
public interface TicketClient {

    @PostMapping("/ticket/")
    TicketDto createTicket(TicketUpsertRequest ticketUpsertRequest);
    
    @GetMapping("/p2p-journeys/{id}")
    P2PJourneyDto getP2PJourneyById(@PathVariable("id") String id);
    
    @GetMapping("/timed-ticket-plans/{id}")
    TimedTicketPlanDto getTimedTicketPlanById(@PathVariable("id") String id);
    
    @PostMapping("/ticket/batch")
    List<TicketDto> createTickets(List<TicketUpsertRequest> ticketRequests);

    /**
     * Fallback implementation used when ticket-service is unreachable (e.g. local dev)
     */
    class TicketClientFallback implements TicketClient {

        private static final double DEFAULT_PRICE = 10000.0;
        private final Throwable cause;

        public TicketClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public TicketDto createTicket(TicketUpsertRequest ticketUpsertRequest) {
            return TicketDto.builder()
                    .id("DUMMY-TICKET")
                    .ticketType(ticketUpsertRequest.getTicketType())
                    .build();
        }

        @Override
        public P2PJourneyDto getP2PJourneyById(String id) {
            return P2PJourneyDto.builder()
                    .id(id)
                    .basePrice(DEFAULT_PRICE)
                    .build();
        }

        @Override
        public TimedTicketPlanDto getTimedTicketPlanById(String id) {
            return TimedTicketPlanDto.builder()
                    .id(id)
                    .validDuration(30)
                    .basePrice(DEFAULT_PRICE)
                    .build();
        }

        @Override
        public List<TicketDto> createTickets(List<TicketUpsertRequest> ticketRequests) {
            return ticketRequests.stream().map(this::createTicket).toList();
        }
    }

    @Component
    class TicketClientFallbackFactory implements FallbackFactory<TicketClient> {
        @Override
        public TicketClient create(Throwable cause) {
            return new TicketClientFallback(cause);
        }
    }
}