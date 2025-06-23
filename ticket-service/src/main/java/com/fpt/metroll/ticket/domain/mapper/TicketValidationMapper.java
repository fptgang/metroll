package com.fpt.metroll.ticket.domain.mapper;

import com.fpt.metroll.ticket.document.TicketValidation;
import com.fpt.metroll.ticket.domain.dto.TicketValidationCreateRequest;
import com.fpt.metroll.shared.domain.dto.ticket.TicketValidationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketValidationMapper {
    TicketValidationDto toDto(TicketValidation document);

    TicketValidation toDocument(TicketValidationDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "validationTime", expression = "java(java.time.Instant.now())")
    TicketValidation toDocument(TicketValidationCreateRequest request);
}