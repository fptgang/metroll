package com.fpt.metroll.ticket.service;

import com.fpt.metroll.ticket.domain.dto.TicketValidationCreateRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketValidationDto;

import java.util.List;
import java.util.Optional;

public interface TicketValidationService {
    PageDto<TicketValidationDto> findAll(String search, PageableDto pageable);

    Optional<TicketValidationDto> findById(String id);

    TicketValidationDto requireById(String id);

    List<TicketValidationDto> findByTicketId(String ticketId);

    List<TicketValidationDto> findByStationId(String stationId);

    TicketValidationDto validateTicket(TicketValidationCreateRequest request);
}