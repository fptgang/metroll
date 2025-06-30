package com.fpt.metroll.ticket.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDashboardDto {
    private Long totalTickets;
    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByType;
    private Long totalValidations;
    private Map<String, Long> validationsByType;
    private Long todayValidations;
    private Long totalP2PJourneys;
    private Map<String, Long> validationsLast7Days;
    private Instant lastUpdated;
}