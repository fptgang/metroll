package com.fpt.metroll.ticket.service;

import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanCreateRequest;
import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanUpdateRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;

import java.util.Optional;

public interface TimedTicketPlanService {
    PageDto<TimedTicketPlanDto> findAll(String search, PageableDto pageable);

    Optional<TimedTicketPlanDto> findById(String id);

    TimedTicketPlanDto requireById(String id);

    TimedTicketPlanDto create(TimedTicketPlanCreateRequest request);

    TimedTicketPlanDto update(String id, TimedTicketPlanUpdateRequest request);

    void delete(String id);
}