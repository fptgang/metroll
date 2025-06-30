package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.ticket.domain.dto.TicketDashboardDto;
import com.fpt.metroll.ticket.repository.P2PJourneyRepository;
import com.fpt.metroll.ticket.repository.TicketRepository;
import com.fpt.metroll.ticket.repository.TicketValidationRepository;
import com.fpt.metroll.ticket.service.TicketDashboardService;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.shared.domain.enums.ValidationType;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TicketDashboardServiceImpl implements TicketDashboardService {

    private final TicketRepository ticketRepository;
    private final TicketValidationRepository ticketValidationRepository;
    private final P2PJourneyRepository p2PJourneyRepository;

    public TicketDashboardServiceImpl(TicketRepository ticketRepository,
            TicketValidationRepository ticketValidationRepository,
            P2PJourneyRepository p2PJourneyRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketValidationRepository = ticketValidationRepository;
        this.p2PJourneyRepository = p2PJourneyRepository;
    }

    @Override
    public TicketDashboardDto getDashboard() {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        // Get all tickets
        var allTickets = ticketRepository.findAll();

        // Count tickets by status
        Map<String, Long> ticketsByStatus = new HashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            long count = allTickets.stream()
                    .filter(ticket -> ticket.getStatus() == status)
                    .count();
            ticketsByStatus.put(status.name(), count);
        }

        // Count tickets by type
        Map<String, Long> ticketsByType = new HashMap<>();
        for (TicketType type : TicketType.values()) {
            long count = allTickets.stream()
                    .filter(ticket -> ticket.getTicketType() == type)
                    .count();
            ticketsByType.put(type.name(), count);
        }

        // Get all validations
        var allValidations = ticketValidationRepository.findAll();

        // Count validations by type
        Map<String, Long> validationsByType = new HashMap<>();
        for (ValidationType type : ValidationType.values()) {
            long count = allValidations.stream()
                    .filter(validation -> validation.getValidationType() == type)
                    .count();
            validationsByType.put(type.name(), count);
        }

        // Count today's validations
        Instant startOfToday = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfToday = startOfToday.plus(1, ChronoUnit.DAYS);
        long todayValidations = allValidations.stream()
                .filter(validation -> validation.getValidationTime() != null)
                .filter(validation -> validation.getValidationTime().isAfter(startOfToday) &&
                        validation.getValidationTime().isBefore(endOfToday))
                .count();

        // Count validations for last 7 days
        Map<String, Long> validationsLast7Days = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            Instant dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

            long count = allValidations.stream()
                    .filter(validation -> validation.getValidationTime() != null)
                    .filter(validation -> validation.getValidationTime().isAfter(dayStart) &&
                            validation.getValidationTime().isBefore(dayEnd))
                    .count();
            validationsLast7Days.put(date.toString(), count);
        }

        // Get P2P journey count
        long totalP2PJourneys = p2PJourneyRepository.count();

        return TicketDashboardDto.builder()
                .totalTickets((long) allTickets.size())
                .ticketsByStatus(ticketsByStatus)
                .ticketsByType(ticketsByType)
                .totalValidations((long) allValidations.size())
                .validationsByType(validationsByType)
                .todayValidations(todayValidations)
                .totalP2PJourneys(totalP2PJourneys)
                .validationsLast7Days(validationsLast7Days)
                .lastUpdated(Instant.now())
                .build();
    }
}