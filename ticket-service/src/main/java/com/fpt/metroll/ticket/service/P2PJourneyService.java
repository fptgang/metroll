package com.fpt.metroll.ticket.service;

import com.fpt.metroll.ticket.domain.dto.P2PJourneyCreateRequest;
import com.fpt.metroll.ticket.domain.dto.P2PJourneyUpdateRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;

import java.util.Optional;

public interface P2PJourneyService {
    PageDto<P2PJourneyDto> findAll(String search, PageableDto pageable);

    Optional<P2PJourneyDto> findById(String id);

    P2PJourneyDto requireById(String id);

    Optional<P2PJourneyDto> findByStations(String startStationId, String endStationId);

    P2PJourneyDto create(P2PJourneyCreateRequest request);

    P2PJourneyDto update(String id, P2PJourneyUpdateRequest request);

    void delete(String id);
}