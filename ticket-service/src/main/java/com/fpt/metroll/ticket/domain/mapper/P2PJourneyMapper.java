package com.fpt.metroll.ticket.domain.mapper;

import com.fpt.metroll.ticket.document.P2PJourney;
import com.fpt.metroll.ticket.domain.dto.P2PJourneyCreateRequest;
import com.fpt.metroll.ticket.domain.dto.P2PJourneyUpdateRequest;
import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface P2PJourneyMapper {
    P2PJourneyDto toDto(P2PJourney document);

    P2PJourney toDocument(P2PJourneyDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    P2PJourney toDocument(P2PJourneyCreateRequest request);

    default P2PJourney updateFromRequest(P2PJourney document, P2PJourneyUpdateRequest request) {
        if (request.getStartStationId() != null) {
            document.setStartStationId(request.getStartStationId());
        }
        if (request.getEndStationId() != null) {
            document.setEndStationId(request.getEndStationId());
        }
        if (request.getBasePrice() != null) {
            document.setBasePrice(request.getBasePrice());
        }
        if (request.getDistance() != null) {
            document.setDistance(request.getDistance());
        }
        if (request.getTravelTime() != null) {
            document.setTravelTime(request.getTravelTime());
        }
        return document;
    }
}