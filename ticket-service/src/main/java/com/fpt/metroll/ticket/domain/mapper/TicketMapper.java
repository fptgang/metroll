package com.fpt.metroll.ticket.domain.mapper;

import com.fpt.metroll.ticket.document.Ticket;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketDto toDto(Ticket document);

    Ticket toDocument(TicketDto dto);


}