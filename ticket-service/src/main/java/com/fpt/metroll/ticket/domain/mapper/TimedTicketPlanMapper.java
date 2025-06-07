package com.fpt.metroll.ticket.domain.mapper;

import com.fpt.metroll.ticket.document.TimedTicketPlan;
import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanCreateRequest;
import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanUpdateRequest;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TimedTicketPlanMapper {
    TimedTicketPlanDto toDto(TimedTicketPlan document);

    TimedTicketPlan toDocument(TimedTicketPlanDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TimedTicketPlan toDocument(TimedTicketPlanCreateRequest request);

    default TimedTicketPlan updateFromRequest(TimedTicketPlan document, TimedTicketPlanUpdateRequest request) {
        if (request.getName() != null) {
            document.setName(request.getName());
        }
        if (request.getValidDuration() != null) {
            document.setValidDuration(request.getValidDuration());
        }
        if (request.getBasePrice() != null) {
            document.setBasePrice(request.getBasePrice());
        }
        return document;
    }
}