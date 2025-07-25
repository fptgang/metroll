package com.fpt.metroll.subway.service.impl;

import com.fpt.metroll.shared.domain.client.TicketClient;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.subway.document.Station;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;
import com.fpt.metroll.subway.domain.mapper.StationMapper;
import com.fpt.metroll.subway.repository.StationRepository;
import com.fpt.metroll.subway.service.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class StationServiceImpl implements StationService {

    private final TicketClient ticketClient;
    private StationRepository stationRepository;
    private StationMapper stationMapper;
    private MongoHelper mongoHelper;

    @Autowired
    public StationServiceImpl(
            StationRepository stationRepository,
            StationMapper stationMapper,
            MongoHelper mongoHelper,
            TicketClient ticketClient) {
        this.stationRepository = stationRepository;
        this.stationMapper = stationMapper;
        this.mongoHelper = mongoHelper;
        this.ticketClient = ticketClient;
    }

    @Override
    public StationDto getStationByCode(String stationCode) {
        return stationRepository.findByCode(stationCode)
                .map(stationMapper::toDto)
                .orElseThrow(() -> {
                    log.error("[StationService] getStationByCode {} not found", stationCode);
                    return new IllegalArgumentException("Station not found");
                });
    }

    @Override
    public PageDto<StationDto> findAll(StationQueryParam queryParam, PageableDto pageable) {
        var result = mongoHelper.find(query -> buildStationQuery(queryParam), pageable, Station.class)
                .map(stationMapper::toDto);

        return PageMapper.INSTANCE.toPageDTO(result);
    }

    @Override
    public StationDto save(StationDto stationDto) {
        Station station = stationMapper.toEntity(stationDto);

        // Indicate update station - validate status change rules
        if (station.getId() != null) {
            // Get current station to check existing status
            Station existingStation = stationRepository.findById(station.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Station not found"));

            Station.StationStatus currentStatus = existingStation.getStatus();
            Station.StationStatus newStatus = station.getStatus();

            if (currentStatus.equals(Station.StationStatus.SCHEDULED_CLOSURE) && !newStatus.equals(currentStatus)) {
                station.setScheduledClosureAt(null);
            }
            // Special handling: If admin tries to close station, schedule closure instead
            if (newStatus == Station.StationStatus.CLOSED) {
                log.info("Admin requested closure for station {}. Scheduling closure for 1 week from now.",
                        station.getCode());
                station.setStatus(Station.StationStatus.SCHEDULED_CLOSURE);
                station.setScheduledClosureAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // 1 week from now
            }
        }

        station = stationRepository.save(station);
        log.info("[StationService] Saved station code: {}, station {}", station.getCode(), station);
        return stationMapper.toDto(station);
    }


    private Query buildStationQuery(StationQueryParam queryParam) {
        Query query = new Query();
        if (queryParam.getName() != null && !queryParam.getName().isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(queryParam.getName(), "i"));
        }
        if (queryParam.getCode() != null && !queryParam.getCode().isEmpty()) {
            query.addCriteria(Criteria.where("code").is(queryParam.getCode()));
        }
        if (queryParam.getStatus() != null && !queryParam.getStatus().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(queryParam.getStatus()));
        }
        if (queryParam.getLineCode() != null && !queryParam.getLineCode().isEmpty()) {
            query.addCriteria(Criteria.where("lineStationInfos.code").is(queryParam.getLineCode()));
        }

        return query;
    }

    @Override
    public List<StationDto> findAllUnavailableStations() {
        return stationRepository.findAllUnavailableStations()
                .stream()
                .map(stationMapper::toDto)
                .toList();
    }

    /**
     * Scheduled job to process stations scheduled for closure
     * Runs every hour to check if any stations are ready to be closed
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // every hour
    public void processScheduledClosures() {
        log.info("Processing scheduled station closures...");

        // Find all stations scheduled for closure that are ready to be closed
        List<Station> stationsToClose = stationRepository.findByStatusAndScheduledClosureAtBefore(
                Station.StationStatus.SCHEDULED_CLOSURE, Instant.now());

        if (stationsToClose.isEmpty()) {
            log.debug("No stations scheduled for closure at this time");
            return;
        }

        log.info("Found {} stations scheduled for closure", stationsToClose.size());

        for (Station station : stationsToClose) {
            try {
                processStationClosure(station);
            } catch (Exception e) {
                log.error("Failed to process closure for station {}: {}", station.getCode(), e.getMessage(), e);
            }
        }

        log.info("Completed processing scheduled station closures");
    }

    private void processStationClosure(Station station) {
        log.info("Processing scheduled closure for station: {}", station.getCode());

        // Check if there are still valid P2P tickets involving this station
        boolean hasValidTickets = ticketClient.hasValidTicketsForStation(station.getCode());

        if (hasValidTickets) {
            log.warn("Station {} still has valid tickets. Closure postponed for 1 day.", station.getCode());
            // Postpone closure by 1 day
            station.setScheduledClosureAt(Instant.now().plusSeconds(24 * 60 * 60)); // 1 day from now
            stationRepository.save(station);
        } else {
            log.info("No valid tickets found for station {}. Proceeding with closure.", station.getCode());
            // Close the station
            station.setStatus(Station.StationStatus.CLOSED);
            station.setScheduledClosureAt(null); // Clear the scheduled closure time
            stationRepository.save(station);

            // Deactivate P2P journeys for this station
            ticketClient.deactivateP2PJourneyByStation(station.getCode());

            log.info("Successfully closed station: {}", station.getCode());
        }
    }
}
